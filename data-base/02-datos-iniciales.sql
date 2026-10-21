
-- Presentaciones (el peso en gramos es la base de toda conversión)
INSERT INTO presentacion (nombre_envase, peso_gramos) VALUES
    ('Bolsa 250g', 250.000),
    ('Bolsa 500g', 500.000),
    ('Bolsa 1kg', 1000.000),
    ('Bolsa 5kg', 5000.000),
    ('Bolsa 10kg', 10000.000)
ON CONFLICT (nombre_envase) DO NOTHING;

-- Tipos de molienda
INSERT INTO tipo_molienda (nombre_molienda, tipo_cafetera, nivel_granulometria, es_grano_entero) VALUES
    ('Grano Entero', 'Cualquiera', 'N/A', TRUE),
    ('Espresso', 'Máquina Espresso', 'Fina', FALSE),
    ('Italiana', 'Moka / Volturno', 'Media-Fina', FALSE),
    ('Filtro', 'V60 / Chemex / Melitta', 'Media', FALSE)
ON CONFLICT (nombre_molienda) DO NOTHING;

-- Canales de venta
INSERT INTO canal_venta (nombre_canal, requiere_datos_fiscales, permite_cta_cte) VALUES
    ('Mostrador / Take Away', FALSE, FALSE),
    ('Instagram / WhatsApp', FALSE, FALSE),
    ('Mayorista (B2B)', TRUE, TRUE)
ON CONFLICT (nombre_canal) DO NOTHING;

-- Medios de pago
INSERT INTO medio_pago (nombre_medio, requiere_comprobante) VALUES
    ('Efectivo', FALSE),
    ('Mercado Pago (Transferencia)', TRUE),
    ('Mercado Pago (QR)', FALSE),
    ('Tarjeta de Crédito', FALSE),
    ('Transferencia Bancaria', TRUE)
ON CONFLICT (nombre_medio) DO NOTHING;

-- Perfiles de tueste
INSERT INTO perfil_tueste (nombre_perfil, temp_objetivo_c, tiempo_tueste_min, merma_esperada_pct, horas_reposo_minimas) VALUES
    ('Tueste Claro', 196.00, 10, 13.00, 48),
    ('Tueste Medio', 205.00, 12, 15.00, 48),
    ('Tueste Oscuro', 215.00, 14, 18.00, 72)
ON CONFLICT (nombre_perfil) DO NOTHING;

-- Cliente genérico para ventas de mostrador sin datos del comprador
INSERT INTO cliente (tipo_cliente, nombre_razon_social, cuit_o_dni, condicion_iva)
SELECT 'B2C', 'Consumidor Final', '00000000', 'Consumidor Final'
WHERE NOT EXISTS (
    SELECT 1 FROM cliente WHERE tipo_cliente = 'B2C' AND nombre_razon_social = 'Consumidor Final'
);
