package com.audit.infrastructure.adapters.output.export.helper;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class DocumentAuditTranslationHelper {

    private static final Map<String, String> OPERATION_LABELS = Map.of(
            "CREATE", "Creación",
            "UPDATE", "Actualización",
            "APPROVE", "Aprobación",
            "VOID", "Anulación",
            "DELETE", "Eliminación");

    private static final Map<String, String> FACTURE_TYPE_LABELS = Map.of(
            "PURCHASE", "Compra",
            "SALE", "Venta",
            "RETURN_ON_SALE", "Devolución en venta",
            "RETURN_ON_PURCHASE", "Devolución en compra",
            "NON_COMMERCIAL_ENTRY", "Entrada no comercial",
            "NON_COMMERCIAL_EXIT", "Salida no comercial");

    private static final Map<String, String> RETURN_TYPE_LABELS = Map.of(
            "RETURN_ON_SALE", "Devolución en venta",
            "RETURN_ON_PURCHASE", "Devolución en compra");

    private static final Map<String, String> MODULE_LABELS = Map.of(
            "WALLET", "Cartera",
            "INOVICES", "Facturas",
            "TREASURY", "Tesorería",
            "ACCOUNTING", "Contabilidad");

    // ── Configuración de inventario ───────────────────────────────
    private static final Map<String, String> INVENTORY_CONFIG_LABELS = Map.of(
            "PEPS", "PEPS (Primeras en entrar, primeras en salir)",
            "WEIGHTED_AVERAGE", "Promedio ponderado");

    private static final Map<String, String> HEADER_FIELD_LABELS = Map.ofEntries(
            Map.entry("factureType", "Tipo de documento"),
            Map.entry("expirationDate", "Fecha de vencimiento"),
            Map.entry("accountingAccount", "Cuenta contable"),
            Map.entry("documentCode", "Código documento"),
            Map.entry("originalFactCode", "Código documento original"),
            Map.entry("returnType", "Tipo de devolución"),
            Map.entry("voidedDocument", "Documento anulado"));

    private static final Map<String, String> DETAIL_FIELD_LABELS = Map.ofEntries(
            Map.entry("productId", "ID Producto"),
            Map.entry("description", "Descripción"),
            Map.entry("amount", "Cantidad"),
            Map.entry("unitPrice", "Precio unitario"),
            Map.entry("discount", "Descuento (%)"),
            Map.entry("taxPercentage", "Impuesto (%)"),
            Map.entry("subtotal", "Subtotal"),
            Map.entry("quantity", "Cantidad"),
            Map.entry("reason", "Motivo de devolución"));

    private static final Map<String, String> TOTALS_FIELD_LABELS = Map.of(
            "totalValue", "Valor total",
            "totalPay", "Total a pagar",
            "pendingValue", "Valor pendiente",
            "totalDiscount", "Total descuentos",
            "totalTax", "Total impuestos");

    private static final Map<String, String> METADATA_FIELD_LABELS = Map.of(
            "productCount", "Cantidad de productos",
            "inventoryConfig", "Configuración de inventario",
            "paymentMethod", "Método de pago",
            "documentStatus", "Estado del documento",
            "voidedDocument", "Documento anulado");

    private static final Map<String, String> VALUE_LABELS;
    static {
        Map<String, String> m = new HashMap<>();
        m.putAll(FACTURE_TYPE_LABELS);
        m.putAll(RETURN_TYPE_LABELS);
        m.putAll(INVENTORY_CONFIG_LABELS);
        m.put("true", "Activo");
        m.put("false", "Inactivo");
        VALUE_LABELS = Collections.unmodifiableMap(m);
    }

    public static String translateOperation(String value) {
        return OPERATION_LABELS.getOrDefault(value, value);
    }

    public static String translateModule(String value) {
        return MODULE_LABELS.getOrDefault(value, value);
    }

    public static String translateHeaderField(String key) {
        return HEADER_FIELD_LABELS.getOrDefault(key, key);
    }

    public static String translateDetailField(String key) {
        return DETAIL_FIELD_LABELS.getOrDefault(key, key);
    }

    public static String translateTotalsField(String key) {
        return TOTALS_FIELD_LABELS.getOrDefault(key, key);
    }

    public static String translateMetadataField(String key) {
        return METADATA_FIELD_LABELS.getOrDefault(key, key);
    }

    public static String translateValue(Object value) {
        if (value == null)
            return "—";
        if (value instanceof Boolean b)
            return b ? "Activo" : "Inactivo";
        if (value instanceof List<?> list) {
            return list.stream()
                    .map(v -> VALUE_LABELS.getOrDefault(String.valueOf(v), String.valueOf(v)))
                    .collect(java.util.stream.Collectors.joining(", "));
        }
        return VALUE_LABELS.getOrDefault(String.valueOf(value), String.valueOf(value));
    }
}
