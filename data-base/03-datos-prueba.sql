-- ---------------------------------------------------------------------
-- ORÍGENES (stock mínimo en gramos)
-- ---------------------------------------------------------------------
INSERT INTO origen_cafe (pais, region, variedad, altura_msnm, notas_cata, stock_minimo_alerta_gramos) VALUES
    ('Colombia', 'Huila', 'Caturra', 1750, 'Caramelo, cítrico suave y chocolate', 5000),
    ('Brasil', 'Santos', 'Bourbon', 1100, 'Nuez, cacao y dulzor prolongado', 10000),
    ('Panamá', 'Boquete', 'Geisha', 1800, 'Floral, jazmín y frutas tropicales', 3000),
    ('Etiopía', 'Yirgacheffe', 'Heirloom', 2000, 'Bergamota, durazno y té negro', 6000),
    ('Guatemala', 'Antigua', 'Bourbon', 1600, 'Chocolate amargo y especias', 8000);

-- ---------------------------------------------------------------------
-- PROVEEDORES (CUIT ficticios)
-- ---------------------------------------------------------------------
INSERT INTO proveedor (cuit, razon_social, pais_origen, telefono_email) VALUES
    ('30-71000001-9', 'Exportadora Huila Café S.A.S.', 'Colombia', 'ventas@huilacafe.example'),
    ('30-71000002-7', 'Santos Coffee Trading Ltda.', 'Brasil', 'comercial@santoscoffee.example'),
    ('30-71000003-5', 'Origen Único Importaciones S.R.L.', 'Argentina', '+54 11 5555-0303');

-- ---------------------------------------------------------------------
-- LOTES DE CAFÉ VERDE
-- stock_actual refleja lo que queda hoy (Huila y Panamá en alerta)
-- ---------------------------------------------------------------------
INSERT INTO lote_cafe_verde (id_origen, id_proveedor, codigo_lote, fecha_ingreso, cant_bolsas, peso_inicial_gramos, stock_actual_gramos)
SELECT o.id_origen, p.id_proveedor, v.codigo, CURRENT_DATE - v.dias, v.bolsas, v.peso, v.stock
FROM (VALUES
    ('Colombia',  'Huila',       '30-71000001-9', 'LV-HUI-001', 70, 2, 70000, 2300),
    ('Brasil',    'Santos',      '30-71000002-7', 'LV-SAN-001', 60, 2, 60000, 12000),
    ('Panamá',    'Boquete',     '30-71000003-5', 'LV-GEI-001', 50, 1, 5000,  0),
    ('Etiopía',   'Yirgacheffe', '30-71000003-5', 'LV-YIR-001', 45, 2, 50000, 18400),
    ('Guatemala', 'Antigua',     '30-71000003-5', 'LV-GUA-001', 40, 2, 40000, 9700)
) AS v(pais, region, cuit, codigo, dias, bolsas, peso, stock)
JOIN origen_cafe o ON o.pais = v.pais AND o.region = v.region
JOIN proveedor p ON p.cuit = v.cuit;

-- ---------------------------------------------------------------------
-- ÓRDENES DE TUESTE (merma real calculada por la base)
-- ---------------------------------------------------------------------
INSERT INTO orden_tueste (id_lote_verde, id_perfil_tueste, fecha_hora_tueste, peso_verde_usado_g, peso_tostado_obtenido_g, operador_tostador, estado_orden)
SELECT lv.id_lote_verde, pt.id_perfil_tueste, (CURRENT_DATE - v.dias) + TIME '10:30', v.verde, v.tostado, 'Mateo', 'COMPLETADO'
FROM (VALUES
    ('LV-HUI-001', 'Tueste Medio',   4, 30000, 25500),
    ('LV-SAN-001', 'Tueste Oscuro', 12, 25000, 20500),
    ('LV-GEI-001', 'Tueste Claro',  26,  5000,  4350),
    ('LV-HUI-001', 'Tueste Medio',  31,  6000,  5100),
    ('LV-YIR-001', 'Tueste Claro',   1, 12000, 10440),
    ('LV-GUA-001', 'Tueste Medio',   6, 14000, 11900)
) AS v(cod_verde, perfil, dias, verde, tostado)
JOIN lote_cafe_verde lv ON lv.codigo_lote = v.cod_verde
JOIN perfil_tueste pt ON pt.nombre_perfil = v.perfil;

-- ---------------------------------------------------------------------
-- LOTES DE CAFÉ TOSTADO
-- ---------------------------------------------------------------------
INSERT INTO lote_cafe_tostado (id_orden_tueste, codigo_lote_tostado, fecha_hora_fin_tueste, fecha_disponible_venta, fecha_caducidad, peso_inicial_gramos, stock_disponible_gramos, stock_reservado_gramos, estado_disponibilidad)
SELECT ot.id_orden_tueste,
       v.codigo,
       ot.fecha_hora_tueste + INTERVAL '12 minutes',
       ot.fecha_hora_tueste + INTERVAL '12 minutes' + INTERVAL '48 hours',
       (CURRENT_DATE - v.dias) + 30,
       ot.peso_tostado_obtenido_g,
       v.disponible,
       0,
       v.estado
FROM (VALUES
    ('LT-1082', 'LV-HUI-001',  4, 'DISPONIBLE', 15250),
    ('LT-1079', 'LV-SAN-001', 12, 'DISPONIBLE', 15500),
    ('LT-1055', 'LV-GEI-001', 26, 'DISPONIBLE',  4350),
    ('LT-1041', 'LV-HUI-001', 31, 'VENCIDO',     5100),
    ('LT-1090', 'LV-YIR-001',  1, 'REPOSO',     10440),
    ('LT-1091', 'LV-GUA-001',  6, 'DISPONIBLE',  5900)
) AS v(codigo, cod_verde, dias, estado, disponible)
JOIN lote_cafe_verde lv ON lv.codigo_lote = v.cod_verde
JOIN orden_tueste ot ON ot.id_lote_verde = lv.id_lote_verde
                    AND ot.fecha_hora_tueste = (CURRENT_DATE - v.dias) + TIME '10:30';

-- ---------------------------------------------------------------------
-- CLIENTES B2B
-- ---------------------------------------------------------------------
INSERT INTO cliente (tipo_cliente, nombre_razon_social, cuit_o_dni, condicion_iva, plazo_cta_cte_dias, limite_credito, contacto_wp_ig, domicilio, localidad) VALUES
    ('B2B', 'Café Cortázar S.A.S.', '30-71234567-8', 'Responsable Inscripto', 30, 500000, '@cafecortazar', 'Av. Hipólito Yrigoyen 4120', 'Lanús'),
    ('B2B', 'La Panadería Central', '30-70987654-3', 'Responsable Inscripto', 15, 200000, '+54 9 11 5555-0101', 'Carlos Pellegrini 880', 'Banfield'),
    ('B2B', 'Hotel Andino', '30-71555222-1', 'Responsable Inscripto', 30, 600000, 'reservas@hotelandino.example', 'Av. Mitre 1500', 'Avellaneda'),
    ('B2C', 'Lucas Benítez', '30111222', 'Consumidor Final', 0, 0, '@lucas.b', NULL, NULL),
    ('B2C', 'Camila Vergara', '33444555', 'Consumidor Final', 0, 0, '+54 9 11 5555-0202', NULL, NULL);

-- ---------------------------------------------------------------------
-- HOJAS DE RUTA, PEDIDOS, FACTURAS, PAGOS Y SUSCRIPCIONES
-- ---------------------------------------------------------------------
DO $$
DECLARE
    c_cortazar INT; c_panaderia INT; c_hotel INT; c_lucas INT; c_camila INT;
    ch_mostrador INT; ch_insta INT; ch_mayorista INT;
    pr_250 INT; pr_1k INT; pr_5k INT;
    mo_grano INT; mo_espresso INT; mo_filtro INT;
    l_1082 INT; l_1079 INT; l_1091 INT;
    mp_efectivo INT; mp_qr INT;
    h1 INT; h2 INT; h3 INT;
    p1 INT; p2 INT; p3 INT; p4 INT; p5 INT;
    s1 INT; s2 INT; s3 INT;
BEGIN
    SELECT id_cliente INTO c_cortazar  FROM cliente WHERE nombre_razon_social = 'Café Cortázar S.A.S.';
    SELECT id_cliente INTO c_panaderia FROM cliente WHERE nombre_razon_social = 'La Panadería Central';
    SELECT id_cliente INTO c_hotel     FROM cliente WHERE nombre_razon_social = 'Hotel Andino';
    SELECT id_cliente INTO c_lucas     FROM cliente WHERE nombre_razon_social = 'Lucas Benítez';
    SELECT id_cliente INTO c_camila    FROM cliente WHERE nombre_razon_social = 'Camila Vergara';

    SELECT id_canal_venta INTO ch_mostrador FROM canal_venta WHERE nombre_canal = 'Mostrador / Take Away';
    SELECT id_canal_venta INTO ch_insta     FROM canal_venta WHERE nombre_canal = 'Instagram / WhatsApp';
    SELECT id_canal_venta INTO ch_mayorista FROM canal_venta WHERE nombre_canal = 'Mayorista (B2B)';

    SELECT id_presentacion INTO pr_250 FROM presentacion WHERE nombre_envase = 'Bolsa 250g';
    SELECT id_presentacion INTO pr_1k  FROM presentacion WHERE nombre_envase = 'Bolsa 1kg';
    SELECT id_presentacion INTO pr_5k  FROM presentacion WHERE nombre_envase = 'Bolsa 5kg';

    SELECT id_tipo_molienda INTO mo_grano    FROM tipo_molienda WHERE nombre_molienda = 'Grano Entero';
    SELECT id_tipo_molienda INTO mo_espresso FROM tipo_molienda WHERE nombre_molienda = 'Espresso';
    SELECT id_tipo_molienda INTO mo_filtro   FROM tipo_molienda WHERE nombre_molienda = 'Filtro';

    SELECT id_lote_tostado INTO l_1082 FROM lote_cafe_tostado WHERE codigo_lote_tostado = 'LT-1082';
    SELECT id_lote_tostado INTO l_1079 FROM lote_cafe_tostado WHERE codigo_lote_tostado = 'LT-1079';
    SELECT id_lote_tostado INTO l_1091 FROM lote_cafe_tostado WHERE codigo_lote_tostado = 'LT-1091';

    SELECT id_medio_pago INTO mp_efectivo FROM medio_pago WHERE nombre_medio = 'Efectivo';
    SELECT id_medio_pago INTO mp_qr       FROM medio_pago WHERE nombre_medio = 'Mercado Pago (QR)';

    -- Hojas de ruta pasadas (ya entregadas)
    INSERT INTO hoja_ruta (fecha_reparto, estado_ruta) VALUES (CURRENT_DATE - 25, 'COMPLETADA') RETURNING id_hoja_ruta INTO h1;
    INSERT INTO hoja_ruta (fecha_reparto, estado_ruta) VALUES (CURRENT_DATE - 35, 'COMPLETADA') RETURNING id_hoja_ruta INTO h2;
    INSERT INTO hoja_ruta (fecha_reparto, estado_ruta) VALUES (CURRENT_DATE - 5,  'COMPLETADA') RETURNING id_hoja_ruta INTO h3;

    -- B2B 1: Café Cortázar - factura vence en 5 días (deuda $250.000)
    INSERT INTO pedido (id_cliente, id_canal_venta, id_hoja_ruta, orden_visita, fecha_hora_pedido, fecha_entrega_prevista, estado_preparacion, estado_cobro, monto_total)
    VALUES (c_cortazar, ch_mayorista, h1, 1, (CURRENT_DATE - 26) + TIME '09:00', CURRENT_DATE - 25, 'ENTREGADO', 'PENDIENTE', 250000)
    RETURNING id_pedido INTO p1;
    INSERT INTO detalle_pedido (id_pedido, id_lote_tostado, id_presentacion, id_tipo_molienda, cantidad_envases, peso_total_gramos, precio_unitario, subtotal)
    VALUES (p1, l_1082, pr_5k, mo_espresso, 2, 10000, 125000, 250000);
    INSERT INTO factura (id_pedido, tipo_comprobante, numero_comprobante, fecha_emision, fecha_vencimiento, monto_total)
    VALUES (p1, 'Factura A', '0012', CURRENT_DATE - 25, CURRENT_DATE + 5, 250000);

    -- B2B 2: La Panadería Central - factura VENCIDA (deuda $85.000)
    INSERT INTO pedido (id_cliente, id_canal_venta, id_hoja_ruta, orden_visita, fecha_hora_pedido, fecha_entrega_prevista, estado_preparacion, estado_cobro, monto_total)
    VALUES (c_panaderia, ch_mayorista, h2, 1, (CURRENT_DATE - 36) + TIME '09:00', CURRENT_DATE - 35, 'ENTREGADO', 'PENDIENTE', 85000)
    RETURNING id_pedido INTO p2;
    INSERT INTO detalle_pedido (id_pedido, id_lote_tostado, id_presentacion, id_tipo_molienda, cantidad_envases, peso_total_gramos, precio_unitario, subtotal)
    VALUES (p2, l_1079, pr_5k, mo_filtro, 1, 5000, 85000, 85000);
    INSERT INTO factura (id_pedido, tipo_comprobante, numero_comprobante, fecha_emision, fecha_vencimiento, monto_total)
    VALUES (p2, 'Factura A', '0010', CURRENT_DATE - 35, CURRENT_DATE - 20, 85000);

    -- B2B 3: Hotel Andino - al día (deuda $120.000)
    INSERT INTO pedido (id_cliente, id_canal_venta, id_hoja_ruta, orden_visita, fecha_hora_pedido, fecha_entrega_prevista, estado_preparacion, estado_cobro, monto_total)
    VALUES (c_hotel, ch_mayorista, h3, 1, (CURRENT_DATE - 6) + TIME '09:00', CURRENT_DATE - 5, 'ENTREGADO', 'PENDIENTE', 120000)
    RETURNING id_pedido INTO p3;
    INSERT INTO detalle_pedido (id_pedido, id_lote_tostado, id_presentacion, id_tipo_molienda, cantidad_envases, peso_total_gramos, precio_unitario, subtotal)
    VALUES (p3, l_1091, pr_5k, mo_filtro, 1, 5000, 120000, 120000);
    INSERT INTO factura (id_pedido, tipo_comprobante, numero_comprobante, fecha_emision, fecha_vencimiento, monto_total)
    VALUES (p3, 'Factura A', '0015', CURRENT_DATE - 5, CURRENT_DATE + 25, 120000);

    -- B2C 1: Lucas por Instagram, Mercado Pago QR
    INSERT INTO pedido (id_cliente, id_canal_venta, fecha_hora_pedido, fecha_entrega_prevista, estado_preparacion, estado_cobro, monto_total)
    VALUES (c_lucas, ch_insta, CURRENT_TIMESTAMP - INTERVAL '2 hours', CURRENT_DATE, 'ENTREGADO', 'PAGADO', 9500)
    RETURNING id_pedido INTO p4;
    INSERT INTO detalle_pedido (id_pedido, id_lote_tostado, id_presentacion, id_tipo_molienda, cantidad_envases, peso_total_gramos, precio_unitario, subtotal)
    VALUES (p4, l_1082, pr_250, mo_filtro, 1, 250, 9500, 9500);
    INSERT INTO pago (id_pedido, id_medio_pago, fecha_hora_pago, monto_pagado)
    VALUES (p4, mp_qr, CURRENT_TIMESTAMP - INTERVAL '2 hours', 9500);

    -- B2C 2: Camila en el mostrador, efectivo
    INSERT INTO pedido (id_cliente, id_canal_venta, fecha_hora_pedido, fecha_entrega_prevista, estado_preparacion, estado_cobro, monto_total)
    VALUES (c_camila, ch_mostrador, CURRENT_TIMESTAMP - INTERVAL '1 hour', CURRENT_DATE, 'ENTREGADO', 'PAGADO', 32000)
    RETURNING id_pedido INTO p5;
    INSERT INTO detalle_pedido (id_pedido, id_lote_tostado, id_presentacion, id_tipo_molienda, cantidad_envases, peso_total_gramos, precio_unitario, subtotal)
    VALUES (p5, l_1091, pr_1k, mo_espresso, 1, 1000, 32000, 32000);
    INSERT INTO pago (id_pedido, id_medio_pago, fecha_hora_pago, monto_pagado)
    VALUES (p5, mp_efectivo, CURRENT_TIMESTAMP - INTERVAL '1 hour', 32000);

    -- Suscripciones. proxima_fecha_generacion = hoy, para poder probar el generador.
    -- dia_entrega se calcula para que coincida con la fecha de hoy.

    -- S1: Café Cortázar, semanal, activa
    INSERT INTO suscripcion (id_cliente, frecuencia, dia_entrega, fecha_inicio, fecha_fin, proxima_fecha_generacion, estado)
    VALUES (c_cortazar, 'SEMANAL', EXTRACT(ISODOW FROM CURRENT_DATE)::INT, CURRENT_DATE - 60, NULL, CURRENT_DATE, 'ACTIVA')
    RETURNING id_suscripcion INTO s1;
    INSERT INTO detalle_suscripcion (id_suscripcion, id_presentacion, id_tipo_molienda, cantidad_envases, precio_unitario)
    VALUES (s1, pr_5k, mo_espresso, 1, 125000);

    -- S2: Hotel Andino, mensual, activa (dos líneas)
    INSERT INTO suscripcion (id_cliente, frecuencia, dia_entrega, fecha_inicio, fecha_fin, proxima_fecha_generacion, estado)
    VALUES (c_hotel, 'MENSUAL', LEAST(EXTRACT(DAY FROM CURRENT_DATE)::INT, 28), CURRENT_DATE - 90, NULL, CURRENT_DATE, 'ACTIVA')
    RETURNING id_suscripcion INTO s2;
    INSERT INTO detalle_suscripcion (id_suscripcion, id_presentacion, id_tipo_molienda, cantidad_envases, precio_unitario)
    VALUES (s2, pr_5k, mo_filtro, 2, 120000),
           (s2, pr_1k, mo_grano, 3, 28000);

    -- S3: La Panadería Central, mensual, PAUSADA (no debe generar pedidos)
    INSERT INTO suscripcion (id_cliente, frecuencia, dia_entrega, fecha_inicio, fecha_fin, proxima_fecha_generacion, estado)
    VALUES (c_panaderia, 'MENSUAL', LEAST(EXTRACT(DAY FROM CURRENT_DATE)::INT, 28), CURRENT_DATE - 120, NULL, (CURRENT_DATE + INTERVAL '1 month')::DATE, 'PAUSADA')
    RETURNING id_suscripcion INTO s3;
    INSERT INTO detalle_suscripcion (id_suscripcion, id_presentacion, id_tipo_molienda, cantidad_envases, precio_unitario)
    VALUES (s3, pr_5k, mo_filtro, 1, 85000);
END $$;
