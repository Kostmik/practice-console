package org.example.util;

import java.util.Scanner;

/**
 * Утилитный класс для безопасного ввода данных с консоли.
 * Перехватывает некорректный ввод и просит пользователя повторить.
 */
public class InputHelper {

    /**
     * Безопасное чтение double с подсказкой.
     * Цикл повторяется, пока пользователь не введёт корректное число.
     */
    public static double readDouble(Scanner sc, String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = sc.nextLine().trim();

            // Проверка на пустой ввод
            if (input.isEmpty()) {
                System.out.println("   ⚠ Ввод не может быть пустым. Попробуйте снова.");
                continue;
            }

            // Заменяем запятую на точку для удобства (русская локаль)
            input = input.replace(',', '.');

            try {
                return Double.parseDouble(input);
            } catch (NumberFormatException e) {
                System.out.println("   ⚠ Некорректное число. Введите значение в формате 10,8");
            }
        }
    }

    /**
     * Безопасное чтение double с проверкой на положительность.
     */
    public static double readPositiveDouble(Scanner sc, String prompt) {
        while (true) {
            double value = readDouble(sc, prompt);
            if (value <= 0) {
                System.out.println("   ⚠ Значение должно быть положительным. Попробуйте снова.");
            } else {
                return value;
            }
        }
    }

    /**
     * Безопасное чтение int с подсказкой.
     */
    public static int readInt(Scanner sc, String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = sc.nextLine().trim();

            if (input.isEmpty()) {
                System.out.println("   ⚠ Ввод не может быть пустым. Попробуйте снова.");
                continue;
            }

            try {
                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("   ⚠ Некорректное целое число. Введите, например, 1 или 2.");
            }
        }
    }

    /**
     * Безопасное чтение int из диапазона [min, max].
     * Используется для выбора пунктов меню.
     */
    public static int readIntInRange(Scanner sc, String prompt, int min, int max) {
        while (true) {
            int value = readInt(sc, prompt);
            if (value < min || value > max) {
                System.out.printf("   ⚠ Значение должно быть от %d до %d. Попробуйте снова.%n", min, max);
            } else {
                return value;
            }
        }
    }
}