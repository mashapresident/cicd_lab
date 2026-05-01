"""String utility functions for demonstrating GitHub Actions CI/CD."""


def reverse_string(s: str) -> str:
    """Reverse a string."""
    return s[::-1]


def count_vowels(s: str) -> int:
    """Count the number of vowels in a string."""
    return sum(1 for char in s.lower() if char in "aeiou")


def is_palindrome(s: str) -> bool:
    """Check if a string is a palindrome (ignores case and spaces)."""
    cleaned = "".join(s.lower().split())
    return cleaned == cleaned[::-1]


def word_frequency(text: str) -> dict:
    """Count frequency of each word in text."""
    words = text.lower().split()
    frequency = {}
    for word in words:
        word = word.strip(".,!?;:")
        frequency[word] = frequency.get(word, 0) + 1
    return frequency


def capitalize_words(s: str) -> str:
    """Capitalize the first letter of each word."""
    return " ".join(word.capitalize() for word in s.split())


def truncate(s: str, max_length: int, suffix: str = "...") -> str:
    """Truncate string to max_length, appending suffix if truncated."""
    if len(s) <= max_length:
        return s
    return s[: max_length - len(suffix)] + suffix
