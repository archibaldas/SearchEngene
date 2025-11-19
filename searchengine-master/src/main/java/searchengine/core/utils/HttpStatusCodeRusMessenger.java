package searchengine.core.utils;

import lombok.experimental.UtilityClass;

@UtilityClass
public class HttpStatusCodeRusMessenger {

    public static String getMessageByCode(int code){
        return switch (code) {
            case 400 -> "Недопустимый запрос.";
            case 401 -> "Запрос не был авторизован.";
            case 403 -> "Доступ к ресурсу запрещен. ";
            case 404 -> "Запрашиваемый ресурс не найден на сервере. ";
            case 500 -> "Внутренняя ошибка сервера. ";
            case 502 -> "Некорректный ответ от вышестоящего сервера. ";
            case 503 -> "Сервис временно недоступен, часто из-за перегрузки.";
            default -> "Код ошибки не известен программе: " + code + ".Требуется обновление данных ответов по кодам ошибок.";
        };
    }
}
