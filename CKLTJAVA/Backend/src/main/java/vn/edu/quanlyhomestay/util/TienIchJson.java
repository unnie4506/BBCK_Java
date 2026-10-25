package vn.edu.quanlyhomestay.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Locale;
import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.LocalDateTime;

public final class TienIchJson {
    private static final Gson GSON = taoGson();

    private TienIchJson() {
    }

    public static Gson taoGson() {
        return new GsonBuilder().serializeNulls()
                .registerTypeAdapter(LocalDate.class,
                        (com.google.gson.JsonSerializer<LocalDate>) (ngay, loai,
                                boiCanh) -> new com.google.gson.JsonPrimitive(ngay.toString()))
                .registerTypeAdapter(LocalDate.class,
                        (com.google.gson.JsonDeserializer<LocalDate>) (json, loai, boiCanh) -> LocalDate
                                .parse(json.getAsString()))
                .registerTypeAdapter(LocalDateTime.class,
                        (com.google.gson.JsonSerializer<LocalDateTime>) (thoiDiem, loai,
                                boiCanh) -> new com.google.gson.JsonPrimitive(thoiDiem.toString()))
                .registerTypeAdapter(LocalDateTime.class, (com.google.gson.JsonDeserializer<LocalDateTime>) (json, loai,
                        boiCanh) -> LocalDateTime.parse(json.getAsString()))
                .create();
    }

    public static <T> T docThanJson(HttpServletRequest yeuCau, Type loai) throws IOException, JsonParseException {
        return GSON.fromJson(yeuCau.getReader(), loai);
    }

    public static <T> T docThanJson(HttpServletRequest yeuCau, Class<T> loai) throws IOException, JsonParseException {
        return GSON.fromJson(yeuCau.getReader(), loai);
    }

    public static void ghiPhanHoiJson(HttpServletResponse phanHoi, int trangThai, PhanHoiApi thanJson)
            throws IOException {
        phanHoi.setStatus(trangThai);
        phanHoi.setCharacterEncoding("UTF-8");
        phanHoi.setContentType("application/json;charset=UTF-8");
        phanHoi.setHeader("Cache-Control", "no-store");
        phanHoi.getWriter().write(GSON.toJson(thanJson));
    }

    public static boolean laNoiDungJson(HttpServletRequest yeuCau) {
        String contentType = yeuCau.getContentType();
        if (contentType == null) {
            return false;
        }
        String mediaType = contentType.split(";", 2)[0].trim().toLowerCase(Locale.ROOT);
        return "application/json".equals(mediaType);
    }

    public static boolean yeuCauNoiDungJson(HttpServletRequest yeuCau, HttpServletResponse phanHoi) throws IOException {
        if (!laNoiDungJson(yeuCau)) {
            ghiPhanHoiJson(phanHoi, HttpServletResponse.SC_BAD_REQUEST,
                    PhanHoiApi.thatBai("Content-Type phải là application/json."));
            return false;
        }
        return true;
    }
}
