-- Presentaciones de Café
INSERT INTO presentacion (nombre_envase, peso_gramos) VALUES 
('Bolsa 250g', 250.000),
('Bolsa 500g', 500.000),
('Bolsa 1kg', 1000.000);

-- Tipos de Molienda
INSERT INTO tipo_molienda (nombre_molienda, tipo_cafetera, nivel_granulometria, es_grano_entero) VALUES 
('Grano Entero', 'Cualquiera', 'N/A', TRUE),
('Espresso', 'Máquina Espresso', 'Fina', FALSE),
('Italiana', 'Moka / Volturno', 'Media-Fina', FALSE),
('Filtro', 'V60 / Chemex / Melitta', 'Media', FALSE),
('Prensa Francesa', 'Prensa Francesa', 'Gruesa', FALSE);

-- Canales de Venta
INSERT INTO canal_venta (nombre_canal, requiere_datos_fiscales, permite_cta_cte) VALUES 
('Mostrador / Take Away', FALSE, FALSE),
('Tienda Online (B2C)', FALSE, FALSE),
('Mayorista (B2B)', TRUE, TRUE);

-- Medios de Pago
INSERT INTO medio_pago (nombre_medio, requiere_comprobante) VALUES 
('Efectivo', FALSE),
('Mercado Pago (Transferencia)', TRUE),
('Mercado Pago (QR)', FALSE),
('Tarjeta de Crédito', FALSE),
('Transferencia Bancaria', TRUE);