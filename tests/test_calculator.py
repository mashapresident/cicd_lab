"""Tests for calculator module."""

import sys  # noqa: E402
import os  # noqa: E402

import pytest  # noqa: E402

sys.path.insert(0, os.path.join(os.path.dirname(__file__), ".."))

from src.calculator import (  # noqa: E402
    add,
    subtract,
    multiply,
    divide,
    power,
    factorial,
    is_prime,
    fibonacci,
)


class TestAdd:
    def test_add_positive_numbers(self):
        assert add(2, 3) == 5

    def test_add_negative_numbers(self):
        assert add(-2, -3) == -5

    def test_add_float_numbers(self):
        assert add(1.5, 2.5) == 4.0

    def test_add_zero(self):
        assert add(0, 5) == 5

    def test_add_large_numbers(self):
        assert add(1_000_000, 2_000_000) == 3_000_000


class TestSubtract:
    def test_subtract_basic(self):
        assert subtract(10, 3) == 7

    def test_subtract_negative_result(self):
        assert subtract(3, 10) == -7

    def test_subtract_same_numbers(self):
        assert subtract(5, 5) == 0

    def test_subtract_floats(self):
        assert abs(subtract(3.5, 1.5) - 2.0) < 1e-9


class TestMultiply:
    def test_multiply_positive(self):
        assert multiply(4, 5) == 20

    def test_multiply_by_zero(self):
        assert multiply(999, 0) == 0

    def test_multiply_negative(self):
        assert multiply(-3, 4) == -12

    def test_multiply_both_negative(self):
        assert multiply(-3, -4) == 12


class TestDivide:
    def test_divide_basic(self):
        assert divide(10, 2) == 5

    def test_divide_float_result(self):
        assert divide(7, 2) == 3.5

    def test_divide_by_zero_raises(self):
        with pytest.raises(ValueError, match="Cannot divide by zero"):
            divide(5, 0)

    def test_divide_negative(self):
        assert divide(-10, 2) == -5


class TestPower:
    def test_power_basic(self):
        assert power(2, 10) == 1024

    def test_power_zero_exp(self):
        assert power(999, 0) == 1

    def test_power_fraction(self):
        assert power(4, 0.5) == 2.0


class TestFactorial:
    def test_factorial_zero(self):
        assert factorial(0) == 1

    def test_factorial_one(self):
        assert factorial(1) == 1

    def test_factorial_five(self):
        assert factorial(5) == 120

    def test_factorial_ten(self):
        assert factorial(10) == 3628800

    def test_factorial_negative_raises(self):
        with pytest.raises(ValueError):
            factorial(-1)

    def test_factorial_non_integer_raises(self):
        with pytest.raises(TypeError):
            factorial(3.5)


class TestIsPrime:
    @pytest.mark.parametrize(
        "n,expected",
        [
            (2, True),
            (3, True),
            (5, True),
            (7, True),
            (11, True),
            (13, True),
            (97, True),
        ],
    )
    def test_prime_numbers(self, n, expected):
        assert is_prime(n) == expected

    @pytest.mark.parametrize(
        "n,expected",
        [
            (0, False),
            (1, False),
            (4, False),
            (9, False),
            (15, False),
            (100, False),
        ],
    )
    def test_non_prime_numbers(self, n, expected):
        assert is_prime(n) == expected


class TestFibonacci:
    def test_fibonacci_zero(self):
        assert fibonacci(0) == []

    def test_fibonacci_one(self):
        assert fibonacci(1) == [0]

    def test_fibonacci_five(self):
        assert fibonacci(5) == [0, 1, 1, 2, 3]

    def test_fibonacci_ten(self):
        assert fibonacci(10) == [0, 1, 1, 2, 3, 5, 8, 13, 21, 34]

    def test_fibonacci_negative(self):
        assert fibonacci(-5) == []