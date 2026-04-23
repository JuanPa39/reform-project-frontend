package co.edu.unipiloto.stationadviser.network;

import co.edu.unipiloto.stationadviser.network.models.*;
import retrofit2.Call;
import retrofit2.http.*;

import java.util.List;

public interface ApiService {
    // Auth
    @POST("auth/login")
    Call<LoginResponse> login(@Body LoginRequest request);

    @POST("auth/register")
    Call<MensajeResponse> register(@Body RegisterRequest request);

    @POST("auth/reset-password")
    Call<MensajeResponse> resetPassword(@Query("email") String email, @Query("newPassword") String newPassword);

    // Estaciones
    @GET("estaciones")
    Call<List<EstacionResponse>> getEstaciones();

    @POST("estaciones")
    Call<EstacionResponse> crearEstacion(@Body EstacionRequest request);

    @PUT("estaciones/{id}")
    Call<EstacionResponse> actualizarEstacion(@Path("id") Long id, @Body EstacionRequest request);

    // Precios
    @GET("precios")
    Call<List<PrecioResponse>> getPrecios();

    @POST("precios")
    Call<Void> crearPrecio(@Body PrecioRequest request);

    // Disponibilidad
    @GET("disponibilidad")
    Call<DisponibilidadResponse> getDisponibilidad(
            @Query("combustible") String combustible,
            @Query("tipoVehiculo") String tipoVehiculo,
            @Query("cantidad") Double cantidad
    );

    // Notificaciones
    @GET("notificaciones")
    Call<List<NotificacionResponse>> getNotificaciones();

    @POST("notificaciones")
    Call<NotificacionResponse> enviarNotificacion(@Body NotificacionRequest request);

    // Ventas
    @GET("ventas/historial")
    Call<List<VentaResponse>> getHistorialVentas();

    @POST("ventas")
    Call<VentaResponse> registrarVenta(@Body VentaRequest request);  // ← CAMBIADO

    // Inventario
    @POST("inventario")
    Call<InventarioResponse> registrarInventario(@Body InventarioRequest request);

    @GET("inventario/disponibilidad")
    Call<List<DisponibilidadResponse>> getDisponibilidad();

    // Normativas
    @GET("normativas")
    Call<List<NormativaResponse>> getNormativas();

    @GET("auditoria")
    Call<List<AuditoriaResponse>> getAuditoria();

    @GET("reportes/consumo-por-zona")
    Call<List<ReporteZonaResponse>> getConsumoPorZona(
            @Query("fechaInicio") String fechaInicio,
            @Query("fechaFin") String fechaFin
    );
    @GET("usuarios")
    Call<List<UsuarioResponse>> getUsuarios();

    @PUT("admin/usuarios/{usuarioId}/estacion")
    Call<Void> asignarEstacion(@Path("usuarioId") Long usuarioId, @Body AsignarEstacionRequest request);
}