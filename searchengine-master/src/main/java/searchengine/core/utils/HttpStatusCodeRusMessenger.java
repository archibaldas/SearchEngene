package searchengine.core.utils;

import lombok.experimental.UtilityClass;

@UtilityClass
public class HttpStatusCodeRusMessenger {

    public static String getMessageByCode(int code){
        return switch (code) {
            case 400 -> code + " : Недопустимый запрос.";
            case 401 -> code + " : Запрос не был авторизован.";
            case 403 -> code + " : Доступ к ресурсу запрещен. ";
            case 404 -> code + " : Запрашиваемый ресурс не найден на сервере. ";
            case 500 -> code + " : Внутренняя ошибка сервера. ";
            case 502 -> code + " : Некорректный ответ от вышестоящего сервера. ";
            case 503 -> code + " : Сервис временно недоступен, из-за частой перегрузки.";
            default -> "Код ошибки не известен программе: " + code + ".Требуется обновление данных ответов по кодам ошибок.";
        };
    }
}
