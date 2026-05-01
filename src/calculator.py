"""Simple calculator module for demonstrating GitHub Actions CI/CD."""


def add(a: float, b: float) -> float:
    """Add two numbers."""
    return a + b


def subtract(a: float, b: float) -> float:
    """Subtract b from a."""
    return a - b


def multiply(a: float, b: float) -> float:
    """Multiply two numbers."""
    return a * b


def divide(a: float, b: float) -> float:
    """Divide a by b. Raises ValueError if b is zero."""
    if b == 0:
        raise ValueError("Cannot divide by zero")
    return a / b


def power(base: float, exp: float) -> float:
    """Raise base to the power of exp."""
    return base**exp


def factorial(n: int) -> int:
    """Calculate factorial of n. Raises ValueError for negative numbers."""
    if not isinstance(n, int):
        raise TypeError("Factorial requires an integer")
    if n < 0:
        raise ValueError("Factorial is not defined for negative numbers")
    if n == 0:
        return 1
    return n * factorial(n - 1)


def is_prime(n: int) -> bool:
    """Check if n is a prime number."""
    if n < 2:
        return False
    if n == 2:
        return True
    if n % 2 == 0:
        return False
    for i in range(3, int(n**0.5) + 1, 2):
        if n % i == 0:
            return False
    return True


def fibonacci(n: int) -> list:
    """Return first n Fibonacci numbers."""
    if n <= 0:
        return []
    if n == 1:
        return [0]
    sequence = [0, 1]
    for _ in range(2, n):
        sequence.append(sequence[-1] + sequence[-2])
    return sequence
