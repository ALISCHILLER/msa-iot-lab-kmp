#!/usr/bin/env python3
"""Static quality audit for MSA IoT Lab source files.

The checks are dependency-free and intentionally run before Gradle resolves external
libraries: package declaration count, brace balance outside string/comment literals,
KDoc coverage for Kotlin type declarations, max file size, and a few source hygiene guards.
"""
from pathlib import Path
import re
import subprocess
import sys

ROOT = Path(__file__).resolve().parents[1]
SRC = ROOT / "composeApp" / "src"
TYPE_DECLARATION = re.compile(
    r"^(?:public |private |internal |actual |expect |abstract |sealed |data )*"
    r"(class|interface|object|enum class|sealed interface|sealed class|data class)\b"
)
MAX_KOTLIN_FILE_LINES = 220

KOTLIN_TEST_ASSERTIONS = (
    "assertEquals", "assertTrue", "assertFalse", "assertNull", "assertNotNull", "assertIs", "assertFailsWith", "assertContentEquals", "fail",
)


REQUIRED_IMPORTS = {
    "FontWeight.": "import androidx.compose.ui.text.font.FontWeight",
    "TextOverflow.": "import androidx.compose.ui.text.style.TextOverflow",
    "Alignment.": "import androidx.compose.ui.Alignment",
}

SYNTAX_ERROR_MARKERS = (
    "unsupported [literal prefixes and suffixes]",
    "expecting '",
    "expecting \"",
    "unclosed string literal",
    "too many characters in a character literal",
)


def package_count(text: str) -> int:
    return (1 if text.startswith("package ") else 0) + text.count("\npackage ")


def remove_literals_and_comments(text: str) -> str:
    result: list[str] = []
    i = 0
    while i < len(text):
        if text.startswith('"""', i):
            end = text.find('"""', i + 3)
            replacement = " " * (len(text) - i if end == -1 else end + 3 - i)
            result.append(replacement)
            i = len(text) if end == -1 else end + 3
        elif text.startswith("//", i):
            end = text.find("\n", i)
            if end == -1:
                result.append(" " * (len(text) - i))
                i = len(text)
            else:
                result.append(" " * (end - i) + "\n")
                i = end + 1
        elif text.startswith("/*", i):
            end = text.find("*/", i + 2)
            replacement = " " * (len(text) - i if end == -1 else end + 2 - i)
            result.append(replacement)
            i = len(text) if end == -1 else end + 2
        elif text[i] == '"':
            start = i
            i += 1
            escaped = False
            while i < len(text):
                ch = text[i]
                if escaped:
                    escaped = False
                elif ch == "\\":
                    escaped = True
                elif ch == '"':
                    i += 1
                    break
                i += 1
            result.append(" " * (i - start))
        elif text[i] == "'":
            start = i
            i += 1
            escaped = False
            while i < len(text):
                ch = text[i]
                if escaped:
                    escaped = False
                elif ch == "\\":
                    escaped = True
                elif ch == "'":
                    i += 1
                    break
                i += 1
            result.append(" " * (i - start))
        else:
            result.append(text[i])
            i += 1
    return "".join(result)



def expected_package_from_path(path: Path) -> str | None:
    parts = path.parts
    try:
        kotlin_index = parts.index("kotlin")
    except ValueError:
        return None
    package_parts = parts[kotlin_index + 1:-1]
    return ".".join(package_parts) if package_parts else None

def brace_balance(text: str) -> int:
    cleaned = remove_literals_and_comments(text)
    return cleaned.count("{") - cleaned.count("}")


def has_kdoc(lines: list[str], index: int) -> bool:
    prior = "\n".join(lines[max(0, index - 30):index])
    return "/**" in prior and "*/" in prior


def suspicious_compile_errors() -> list[str]:
    kotlinc = subprocess.run(["bash", "-lc", "command -v kotlinc"], capture_output=True, text=True)
    if kotlinc.returncode != 0:
        return []
    kotlin_files = sorted((SRC / "commonMain" / "kotlin").rglob("*.kt")) + sorted((SRC / "commonTest" / "kotlin").rglob("*.kt"))
    command = ["kotlinc", *[str(path) for path in kotlin_files], "-d", "/tmp/msa-iot-lab-parse.jar"]
    result = subprocess.run(command, cwd=ROOT, capture_output=True, text=True)
    findings: list[str] = []
    for line in result.stderr.splitlines():
        lower = line.lower()
        if any(marker in lower for marker in SYNTAX_ERROR_MARKERS):
            findings.append(line)
    return findings


def android_manifest_errors() -> list[str]:
    manifest = ROOT / "composeApp" / "src" / "androidMain" / "AndroidManifest.xml"
    if not manifest.exists():
        return ["AndroidManifest.xml is missing"]
    text = manifest.read_text(encoding="utf-8")
    findings: list[str] = []
    if 'android:allowBackup="false"' not in text:
        findings.append("AndroidManifest.xml: android:allowBackup must be false because profiles may contain sensitive connection data")
    if 'android.permission.INTERNET' not in text:
        findings.append("AndroidManifest.xml: INTERNET permission is required for protocol testing")
    if 'android:networkSecurityConfig="@xml/network_security_config"' not in text:
        findings.append("AndroidManifest.xml: explicit network security config is required for local IoT cleartext testing")
    return findings


def main() -> int:
    errors: list[str] = []
    for generated_path in ROOT.rglob("*"):
        if generated_path.name == "__pycache__" or generated_path.suffix == ".pyc":
            errors.append(f"{generated_path}: generated Python cache file must not be committed")
    kotlin_files = sorted(SRC.rglob("*.kt"))
    declarations = 0
    kdoc_covered = 0

    for path in kotlin_files:
        text = path.read_text(encoding="utf-8")
        count = package_count(text)
        if count != 1:
            errors.append(f"{path}: expected 1 package declaration, found {count}")
        else:
            declared_package = next(line.removeprefix("package ").strip() for line in text.splitlines() if line.startswith("package "))
            expected_package = expected_package_from_path(path)
            if expected_package and declared_package != expected_package:
                errors.append(f"{path}: package '{declared_package}' does not match path package '{expected_package}'")
        line_count = len(text.splitlines())
        if line_count > MAX_KOTLIN_FILE_LINES:
            errors.append(f"{path}: file is too large ({line_count} lines > {MAX_KOTLIN_FILE_LINES}); split by responsibility")
        balance = brace_balance(text)
        if balance != 0:
            errors.append(f"{path}: brace balance is {balance}")
        if "TODO(" in text or "TODO:" in text or "FIXME" in text:
            errors.append(f"{path}: contains TODO/FIXME marker")

        for symbol, required_import in REQUIRED_IMPORTS.items():
            if symbol in text and required_import not in text:
                errors.append(f"{path}: uses {symbol.rstrip('.')} without required import '{required_import}'")
        if "!!" in remove_literals_and_comments(text):
            errors.append(f"{path}: contains non-null assertion (!!); prefer explicit validation or safe calls")
        normalized = str(path).replace("\\", "/")
        if "/ui/" in normalized and "import com.msa.iotlab.database" in text:
            errors.append(f"{path}: UI layer must not import database classes directly")
        if "/ui/" in normalized and "import com.msa.iotlab.platform" in text:
            errors.append(f"{path}: UI layer must not import platform implementation classes directly")
        if "/ui/" in normalized and "import com.msa.iotlab.core.AppClock" in text:
            errors.append(f"{path}: UI layer must not create timestamps directly")
        if "/ui/" in normalized and "import com.msa.iotlab.core.IdGenerator" in text:
            errors.append(f"{path}: UI layer must not generate domain identifiers directly")
        if "/ui/" in normalized and re.search(r"=\s*[A-Za-z0-9_]+UseCase\(", text):
            errors.append(f"{path}: UI layer must receive use cases through the composition root")
        if "/protocol/" in normalized and "import androidx.compose" in text:
            errors.append(f"{path}: protocol layer must not depend on Compose UI")
        if "/commonMain/" in normalized and re.search(r"^import (java|javax|platform)\.", text, re.MULTILINE):
            errors.append(f"{path}: commonMain must not import JVM/iOS platform APIs directly")
        if "/commonTest/" in normalized:
            for assertion in KOTLIN_TEST_ASSERTIONS:
                if f"{assertion}(" in text and f"import kotlin.test.{assertion}" not in text:
                    errors.append(f"{path}: uses kotlin.test.{assertion} without an explicit import")
        lines = text.splitlines()
        for i, line in enumerate(lines):
            if TYPE_DECLARATION.match(line.strip()):
                declarations += 1
                if has_kdoc(lines, i):
                    kdoc_covered += 1
                else:
                    errors.append(f"{path}:{i + 1}: missing KDoc above declaration: {line.strip()}")

    for finding in suspicious_compile_errors():
        errors.append(f"kotlinc syntax marker: {finding}")
    errors.extend(android_manifest_errors())

    print(f"Kotlin files: {len(kotlin_files)}")
    print(f"Type declarations: {declarations}")
    print(f"KDoc-covered declarations: {kdoc_covered}")
    print(f"Errors: {len(errors)}")
    for error in errors:
        print(error)
    return 1 if errors else 0


if __name__ == "__main__":
    sys.exit(main())
