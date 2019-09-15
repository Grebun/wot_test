package com.branch.task.indrive

import com.branch.task.indrive.response.VehiclesResponse
import com.google.gson.Gson
import khttp.get
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * *	Все проверки желательно сопровождать запросами в БД
 *
 *	Обязательные поля:
 *		Запрос с обязательными полями
 *		Запрос без application_id
 *		Запрос с несуществующим application_id
 *		Запрос без account_id
 *		Запрос с несуществующим account_id
 *		Проверки на запрещенные символы
 *
 *	Тестирование access_token:
 *		Запрос с корректным токеном
 *		Запрос с несуществующим токеном
 *		Запрос с пустум токеном
 *
 *	Тестирование fields:
 *		Исключить mark_of_mastery из ответа
 *           Проверить получаемое значение (0), лучше с инсертом в БД
 *           Проверить получаемое значение (1), лучше с инсертом в БД
 *           Проверить получаемое значение (2), лучше с инсертом в БД
 *           Проверить получаемое значение (3), лучше с инсертом в БД
 *           Проверить получаемое значение (4), лучше с инсертом в БД
 *		Исключить tank_id из ответа
 *		Исключить statistics из ответа (Проверить, что блока нет совсем)
 *		Исключить statistics.battles из ответа
 *		Исключить statistics.wins из ответа
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
 *	Тестирование tank_id:
 *		Получить существующий танк
 *		Получить несуществующий танк
 *		Пустой идентификатор
 *		Поиск 100 едениц (Слотов столько скорее всего не будет, но инсертить в базу никто не запрещает)
 *		Поиск 101 едениц (Слотов столько скорее всего не будет, но инсертить в базу никто не запрещает)
 */
class PlayerVehiclesTest {
    private val gson = Gson()

    // Все переменные ниже лучше вынести из хардкода, хотя бы в двумерный Properties
    private val baseUrl = "https://api.worldoftanks.ru/wot/account/tanks/"
    private val baseApplicationId = "5bd514145ee53820490efeeffbe6afb2"
    private val baseUser = "RenamedUser_15181047"
    private val baseUserId = "15181047"

    /**
     * Простой минимальный зарос
     */
    @Test
    fun simple_test() {
        val response = get(
            baseUrl,
            params = mapOf("application_id" to baseApplicationId, "account_id" to baseUserId)
        ).text
        val actualResult = gson.fromJson(response, VehiclesResponse::class.java)

        assertEquals("ok", actualResult.status)
        assertEquals(1, actualResult.meta.count)
        assertEquals(11, actualResult.data[baseUserId]?.size)
    }

    /**
     * Запрос без application_id
     */
    @Test
    fun request_without_application_id() {
        val response = get(
            baseUrl,
            params = mapOf("account_id" to baseUserId)
        ).text
        val actualResult = gson.fromJson(response, VehiclesResponse::class.java)

        assertEquals("error", actualResult.status)
        assertEquals("application_id", actualResult.error.field)
        assertEquals("APPLICATION_ID_NOT_SPECIFIED", actualResult.error.message)
        assertEquals(402, actualResult.error.code)
        assertNull(actualResult.error.value)
    }

    /**
     * Запрос с несуществующим application_id
     */
    @Test
    fun request_with_fake_application_id() {
        val response = get(
            baseUrl,
            params = mapOf("application_id" to "-123", "account_id" to baseUserId)
        ).text
        val actualResult = gson.fromJson(response, VehiclesResponse::class.java)

        assertEquals("error", actualResult.status)
        assertEquals("application_id", actualResult.error.field)
        assertEquals("INVALID_APPLICATION_ID", actualResult.error.message)
        assertEquals(407, actualResult.error.code)
        assertEquals("-123", actualResult.error.value)
    }

    /**
     * Запрос без account_id
     */
    @Test
    fun request_without_account_id() {
        val response = get(
            baseUrl,
            params = mapOf("application_id" to baseApplicationId)
        ).text
        val actualResult = gson.fromJson(response, VehiclesResponse::class.java)

        assertEquals("error", actualResult.status)
        assertEquals("account_id", actualResult.error.field)
        assertEquals("ACCOUNT_ID_NOT_SPECIFIED", actualResult.error.message)
        assertEquals(402, actualResult.error.code)
        assertNull(actualResult.error.value)
    }

    /**
     * Запрос с несуществующим account_id
     */
    @Test
    fun request_with_fake_account_id() {
        val response = get(
            baseUrl,
            params = mapOf("application_id" to baseApplicationId, "account_id" to "-123")
        ).text
        val actualResult = gson.fromJson(response, VehiclesResponse::class.java)

        assertEquals("error", actualResult.status)
        assertEquals("account_id", actualResult.error.field)
        assertEquals("INVALID_ACCOUNT_ID", actualResult.error.message)
        assertEquals(407, actualResult.error.code)
        assertEquals("-123", actualResult.error.value)
    }

    /**
     * Тестирование fields: Исключить mark_of_mastery из ответа
     */
    @Test
    fun request_fields_mastery_excluding() {
        val response = get(
            baseUrl,
            params = mapOf(
                "application_id" to baseApplicationId,
                "account_id" to baseUserId,
                "fields" to "-mark_of_mastery"
            )
        ).text
        val actualResult = gson.fromJson(response, VehiclesResponse::class.java)

        // Проходим по всему списку слотов
        for (item in actualResult.data[baseUserId]!!)
            assertNull(item.mark_of_mastery)
    }

    /**
     * Тестирование tank_id: Получить существующий танк
     */
    @Test
    fun request_by_tank_id() {
        // Такие данные лучше не хранить в тесте и вообще лучше селектить при каждом выполнении
        val tankId = "769"
        val wins = 16
        val battles = 35
        val mark_of_mastery = 2

        val response = get(
            baseUrl,
            params = mapOf("application_id" to baseApplicationId, "account_id" to baseUserId, "tank_id" to tankId)
        ).text
        val actualResult = gson.fromJson(response, VehiclesResponse::class.java)

        assertEquals("ok", actualResult.status)
        assertEquals(1, actualResult.meta.count)

        assertEquals(wins, actualResult.data[baseUserId]!![0].statistics.wins)
        assertEquals(battles, actualResult.data[baseUserId]!![0].statistics.battles)
        assertEquals(mark_of_mastery, actualResult.data[baseUserId]!![0].mark_of_mastery)
        assertEquals(tankId.toInt(), actualResult.data[baseUserId]!![0].tank_id)
    }

}