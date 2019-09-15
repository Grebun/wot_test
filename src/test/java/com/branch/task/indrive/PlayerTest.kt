package com.branch.task.indrive

import com.branch.task.indrive.response.PlayerResponse
import com.google.gson.Gson
import khttp.get
import khttp.post
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 *	Все проверки желательно сопровождать запросами в БД
 *
 *	Обязательные поля:
 *      Запрос с обязательными полями
 *		Запрос без application_id
 *		Запрос с несуществующим application_id
 *		Запрос без search
 *		Запрос с пустой строкой в search
 *		Проверки на запрещенные символы
 *
 *	Тестирование fields:
 *		Исключить nickname из ответа
 *		Исключить account_id из ответа
 *		Переполнить поле больше чем 100 значений
 *		Проверить граничное значение в 100 значений
 *
 *	Тестирование language:
 *		Запросить "ru"
 *		Запросить "en"
 *		Запросить "pl"
 *		Запросить "de"
 *		Запросить "fr"
 *		Запросить "es"
 *		Запросить "zh-cn"
 *		Запросить "zh-tw"
 *		Запросить "tr"
 *		Запросить "cs"
 *		Запросить "th"
 *		Запросить "vi"
 *		Запросить "ko"
 *
 *	Тестирование type:
 *		startswith
 *			Тестирование граничных значений
 *				Логин 2 символа
 *				Логин 25 Символов
 *				Пустой логин
 *				Логин 3 символа
 *				Логин 24 символа
 *			Несуществующий пользователь
 *
 *		exact
 *			Пустой логин
 *			Поиск существующего пользователя
 *			Поиск несуществующего пользователя
 *			Поиск 100 пользователей (существующих и нет)
 *			Поиск 101 пользователя (проверка ограничения)
 *			Проверка регистра
 */
class PlayerTest {
    private val gson = Gson()

    // Все переменные ниже лучше вынести из хардкода, хотя бы в двумерный Properties
    private val baseUrl = "https://api.worldoftanks.ru/wot/account/list/"
    private val baseApplicationId = "5bd514145ee53820490efeeffbe6afb2"
    private val baseUser = "RenamedUser_15181047"
    private val baseUserId = 15181047

    /**
     * Запрос с обязательными полями
     */
    @Test
    fun simple_test() {
        val response = get(
            baseUrl,
            params = mapOf("application_id" to baseApplicationId, "search" to baseUser)
        ).text
        val actualResult = gson.fromJson(response, PlayerResponse::class.java)

        assertEquals("ok", actualResult.status)
        assertEquals(1, actualResult.meta.count)
        assertEquals(baseUser, actualResult.data[0].nickname)
        assertEquals(baseUserId, actualResult.data[0].account_id)
    }

    /**
     * Запрос без application_id
     */
    @Test
    fun request_without_application_id() {
        val response = get(
            baseUrl,
            params = mapOf("search" to baseUser)
        )
        val actualResult = gson.fromJson(response.text, PlayerResponse::class.java)

        assertEquals("error", actualResult.status)
        assertEquals("application_id", actualResult.error.field)
        assertEquals("APPLICATION_ID_NOT_SPECIFIED", actualResult.error.message)
        assertEquals(402, actualResult.error.code)
        assertNull(actualResult.error.value)

        // TODO: Вообще так как нет докуменитации, то проверка ниже покажет баг.
        // В чём проблема вернуть статус код такой же, как и в ответе
        // assertEquals(response.statusCode, actualResult.error.code)
    }

    /**
     * Запрос с несуществующим application_id
     */
    @Test
    fun request_with_fake_application_id() {
        val fakeAppId = "-123"
        val response = get(
            baseUrl,
            params = mapOf("application_id" to fakeAppId, "search" to baseUser)
        ).text
        val actualResult = gson.fromJson(response, PlayerResponse::class.java)

        assertEquals("error", actualResult.status)
        assertEquals("application_id", actualResult.error.field)
        assertEquals("INVALID_APPLICATION_ID", actualResult.error.message)
        assertEquals(407, actualResult.error.code)
        assertEquals(fakeAppId, actualResult.error.value)
    }

    /**
     * Запрос без search
     */
    @Test
    fun request_without_search() {
        val response = get(
            baseUrl,
            params = mapOf("application_id" to baseApplicationId)
        ).text
        val actualResult = gson.fromJson(response, PlayerResponse::class.java)

        assertEquals("error", actualResult.status)
        assertEquals("search", actualResult.error.field)
        assertEquals("SEARCH_NOT_SPECIFIED", actualResult.error.message)
        assertEquals(402, actualResult.error.code)
        assertNull(actualResult.error.value)
    }

    /**
     * Запрос с пустой строкой в search
     */
    @Test
    fun request_with_empty_search() {
        val response = get(
            baseUrl,
            params = mapOf("application_id" to baseApplicationId, "search" to "")
        ).text
        val actualResult = gson.fromJson(response, PlayerResponse::class.java)

        assertEquals("error", actualResult.status)
        assertEquals("search", actualResult.error.field)
        assertEquals("SEARCH_NOT_SPECIFIED", actualResult.error.message)
        assertEquals(402, actualResult.error.code)
        assertEquals("", actualResult.error.value)
    }

    /**
     * Поиск по части логина
     */
    @Test
    fun request_search_with_partially_login() {
        val response = get(
            baseUrl,
            params = mapOf("application_id" to baseApplicationId, "search" to "RenamedUser_")
        ).text
        val actualResult = gson.fromJson(response, PlayerResponse::class.java)

        assertEquals("ok", actualResult.status)
        assertEquals(100, actualResult.meta.count)
        assertEquals(100, actualResult.data.size)
    }

    /**
     * Тестирование fields: Исключить nickname из ответа
     */
    @Test
    fun request_fields_excluding() {
        val response = post(
            baseUrl,
            params = mapOf("application_id" to baseApplicationId, "search" to baseUser, "fields" to "-nickname")
        ).text
        val actualResult = gson.fromJson(response, PlayerResponse::class.java)

        assertEquals("ok", actualResult.status)
        assertEquals(1, actualResult.meta.count)
        assertNull(actualResult.data[0].nickname)
        assertEquals(baseUserId, actualResult.data[0].account_id)
    }

    /**
     * Тестирование language: Запросить "ru"
     * Фактически повтор первого теста
     */
    @Test
    fun request_language_ru() {
        val response = post(
            baseUrl,
            params = mapOf("application_id" to baseApplicationId, "search" to baseUser, "language" to "ru")
        ).text
        val actualResult = gson.fromJson(response, PlayerResponse::class.java)

        assertEquals("ok", actualResult.status)
        assertEquals(1, actualResult.meta.count)
        assertEquals(baseUser, actualResult.data[0].nickname)
        assertEquals(baseUserId, actualResult.data[0].account_id)
    }

    /**
     * Тестирование type: startswith: Тестирование граничных значений-Логин 2 символа
     */
    @Test
    fun request_type_startswith_by_two_symbol() {
        val response = post(
            baseUrl,
            params = mapOf("application_id" to baseApplicationId, "search" to "Re", "type" to "startswith")
        ).text
        val actualResult = gson.fromJson(response, PlayerResponse::class.java)

        assertEquals("error", actualResult.status)
        assertEquals("search", actualResult.error.field)
        assertEquals("NOT_ENOUGH_SEARCH_LENGTH", actualResult.error.message)
        assertEquals(407, actualResult.error.code)
        assertEquals("Re", actualResult.error.value)
    }

    /**
     * Тестирование exact: exact: Поиск 101 пользователя (проверка ограничения)
     */
    @Test
    fun request_seach_type_exact_over_limit() {
        // Список лучше получить из базы
        val listOfUser =
            "$baseUser,RenamedUser_0,RenamedUser_00,RenamedUser_000,RenamedUser_0000,RenamedUser_000000,RenamedUser_0000000,RenamedUser_00000000,RenamedUser_0000000000,RenamedUser_00000000000,RenamedUser_000000000000,RenamedUser_000000000001,RenamedUser_000000000002,RenamedUser_000000000010,RenamedUser_0000000001,RenamedUser_0000000002,RenamedUser_000000001,RenamedUser_000000004,RenamedUser_000000005,RenamedUser_00000001,RenamedUser_000000011,RenamedUser_00000002,RenamedUser_00000003,RenamedUser_00000004,RenamedUser_00000005,RenamedUser_00000006,RenamedUser_00000007,RenamedUser_0000001,RenamedUser_00000011,RenamedUser_0000002,RenamedUser_0000003,RenamedUser_0000004,RenamedUser_0000005,RenamedUser_0000007,RenamedUser_000001,RenamedUser_0000010,RenamedUser_000001003,RenamedUser_00000123,RenamedUser_0000013,RenamedUser_0000021,RenamedUser_000003000005,RenamedUser_000005,RenamedUser_00000777,RenamedUser_00001,RenamedUser_00001002,RenamedUser_00001003,RenamedUser_00001005,RenamedUser_00001006,RenamedUser_00001009,RenamedUser_00001011,RenamedUser_00001013,RenamedUser_00001014,RenamedUser_00001017,RenamedUser_00001019,RenamedUser_00001020,RenamedUser_00001021,RenamedUser_00001022,RenamedUser_00001024,RenamedUser_00001027,RenamedUser_00001030,RenamedUser_00001031,RenamedUser_00001033,RenamedUser_00001035,RenamedUser_00001038,RenamedUser_00001039,RenamedUser_00001040,RenamedUser_00001041,RenamedUser_00001044,RenamedUser_00001045,RenamedUser_00001047,RenamedUser_00001048,RenamedUser_00001049,RenamedUser_00001051,RenamedUser_00001052,RenamedUser_00001056,RenamedUser_00001057,RenamedUser_00001058,RenamedUser_00001059,RenamedUser_00001060,RenamedUser_00001061,RenamedUser_00001063,RenamedUser_00001064,RenamedUser_00001065,RenamedUser_00001066,RenamedUser_00001067,RenamedUser_00001068,RenamedUser_00001069,RenamedUser_00001070,RenamedUser_00001071,RenamedUser_00001074,RenamedUser_00001075,RenamedUser_00001076,RenamedUser_00001079,RenamedUser_00001080,RenamedUser_00001081,RenamedUser_00001082,RenamedUser_00001083,RenamedUser_00001086,RenamedUser_00001087,RenamedUser_00001088,RenamedUser_00001091"
        val response = post(
            baseUrl,
            params = mapOf("application_id" to baseApplicationId, "search" to listOfUser, "type" to "exact")
        ).text
        val actualResult = gson.fromJson(response, PlayerResponse::class.java)

        assertEquals("error", actualResult.status)
        assertEquals("search", actualResult.error.field)
        assertEquals("SEARCH_LIST_LIMIT_EXCEEDED", actualResult.error.message)
        assertEquals(407, actualResult.error.code)
        assertEquals(listOfUser, actualResult.error.value)
    }
}