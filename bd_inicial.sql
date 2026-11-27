-- ============================================================
-- BD INICIAL - verdegreen_grupo9
-- Generada para: MariaDB 10.11 / phpMyAdmin 5.2.3
-- Fecha: 26-11-2026
--
-- CONSIDERACIONES:
--  * tabla pedido: SIN fecha_confirmacion, CON fecha_expiracion_pago
--    (la expiración de 60s del link se guarda en BD y sobrevive reinicios)
--  * tabla producto: SIN stock_minimo / stock_maximo (los límites están en ConfiguracionNegocio)
--  * tabla pago: SIN metodo
--  * tabla cotizacion: estado = SOLICITADA, enlace_whatsapp TEXT, pedido_id nullable
--  * los datos de pedidos, cotizaciones, pagos y detalle_pedido se generan
--    automáticamente al ejecutar los endpoints
--
-- INSTRUCCIONES:
--  1) Borrá la BD 'verdegreen_grupo9' y creala de nuevo vacía
--  2) Seleccionala -> pestaña SQL -> pegá todo esto -> Ejecutar
-- ============================================================

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

-- --------------------------------------------------------
-- Estructura: categoria
-- --------------------------------------------------------
CREATE TABLE `categoria` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `nombre` varchar(100) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

INSERT INTO `categoria` (`id`, `nombre`) VALUES
(1, 'Vasos'),
(2, 'Copas'),
(3, 'Bandejas'),
(4, 'Clamshells');

--
-- Estructura de tabla para la tabla `cliente`
--

CREATE TABLE `cliente` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `razon_social` varchar(150) NOT NULL,
  `cuit` varchar(13) NOT NULL,
  `email` varchar(100) NOT NULL,
  `telefono` varchar(30) DEFAULT NULL,
  `direccion_fiscal` varchar(200) NOT NULL,
  `fecha_alta` date NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `cuit` (`cuit`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci AUTO_INCREMENT=6;


INSERT INTO `cliente` (`id`, `razon_social`, `cuit`, `email`, `telefono`, `direccion_fiscal`, `fecha_alta`) VALUES
(1, 'Verdulería Don José', '20-28456789-3', 'abrilidoeta03@gmail.com', '2954412345', 'Av. San Martín 1250, Santa Rosa', '2026-06-28'),
(2, 'Distribuidora Pampeana S.R.L.', '30-71234567-8', 'abrilidoeta03@gmail.com', '2302423456', 'Calle 15 N° 840, General Pico', '2026-07-28'),
(3, 'Lucía Fernández', '27-351234569-', 'abrilidoeta03@gmail.com', '2994567890', 'Belgrano 455, Neuquén', '2026-09-26'),
(4, 'Comedero Municipal', '20-28455710-9', 'abrilidoeta03@gmail.com', '2302630275', 'Uruguay 2272', '2026-11-26'),
(5, 'Club de Fútbol Pepito', '20-39401639-2', 'abrilidoeta03@gmail.com', '2954720452', 'Pellegrini 3671', '2026-11-26');

-- --------------------------------------------------------
-- Estructura: producto  (SIN stock_minimo / stock_maximo)
-- --------------------------------------------------------
CREATE TABLE `producto` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `categoria_id` bigint(20) NOT NULL,
  `nombre` varchar(150) NOT NULL,
  `precio` decimal(10,2) NOT NULL,
  `stock` int(11) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_producto_categoria` (`categoria_id`),
  CONSTRAINT `fk_producto_categoria` FOREIGN KEY (`categoria_id`) REFERENCES `categoria` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

INSERT INTO `producto` (`id`, `categoria_id`, `nombre`, `precio`, `stock`) VALUES
(1, 1, 'Vaso PET 180 ml', 35.00, 4045),
(2, 1, 'Vaso PET 300 ml', 48.00, 2575),
(3, 1, 'Vaso PET 500 ml', 65.00, 2780),
(4, 2, 'Copa de trago 200 ml', 120.00, 1500),
(5, 2, 'Copa de postre 150 ml', 95.00, 0),
(6, 3, 'Bandeja chica 15x20 cm', 80.00, 2500),
(7, 3, 'Bandeja grande 25x35 cm', 140.00, 1800),
(8, 4, 'Clamshell 500 g', 150.00, 2000),
(9, 4, 'Clamshell 1 kg', 210.00, 1200);

-- --------------------------------------------------------
-- Estructura: promocion
-- --------------------------------------------------------
CREATE TABLE `promocion` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `producto_id` bigint(20) NOT NULL,
  `nombre` varchar(100) NOT NULL,
  `descripcion` varchar(255) DEFAULT NULL,
  `tipo_descuento` varchar(20) NOT NULL,
  `valor_descuento` decimal(10,2) NOT NULL,
  `fecha_inicio` date NOT NULL,
  `fecha_fin` date NOT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_promocion_producto` (`producto_id`),
  CONSTRAINT `fk_promocion_producto` FOREIGN KEY (`producto_id`) REFERENCES `producto` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

INSERT INTO `promocion` (`id`, `producto_id`, `nombre`, `descripcion`, `tipo_descuento`, `valor_descuento`, `fecha_inicio`, `fecha_fin`) VALUES
(1, 2, 'Semana del vaso', '15% off en vasos de 300 ml', 'PORCENTAJE', 15.00, '2026-10-21', '2026-11-05'),
(2, 8, 'Promo repostería', '20% off en clamshells de 500 g', 'PORCENTAJE', 20.00, '2026-10-24', '2026-11-15'),
(3, 5, 'Postres de primavera', '10% off en copas de postre', 'PORCENTAJE', 10.00, '2026-10-25', '2026-11-10'),
(4, 7, 'Liquidación de invierno', '25% off en bandejas grandes', 'PORCENTAJE', 25.00, '2026-08-27', '2026-09-26'),
(5, 3, 'Verano 2027', '20% off en vasos de 500 ml', 'PORCENTAJE', 20.00, '2026-11-25', '2026-12-25');

-- --------------------------------------------------------
-- Estructura: pedido  (SIN fecha_confirmacion, CON fecha_expiracion_pago)
-- --------------------------------------------------------
CREATE TABLE `pedido` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `cliente_id` bigint(20) NOT NULL,
  `estado` varchar(30) NOT NULL,
  `fecha_pedido` date NOT NULL,
  `pais` varchar(60) DEFAULT NULL,
  `codigo_postal` varchar(10) DEFAULT NULL,
  `calle` varchar(100) DEFAULT NULL,
  `altura` int(11) DEFAULT NULL,
  `costo_envio` decimal(10,2) DEFAULT NULL,
  `pedido_especial` bit(1) NOT NULL,
  `total_unidades` int(11) DEFAULT NULL,
  `link_pago` varchar(200) DEFAULT NULL,
  `fecha_expiracion_pago` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_pedido_cliente` (`cliente_id`),
  CONSTRAINT `fk_pedido_cliente` FOREIGN KEY (`cliente_id`) REFERENCES `cliente` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------
-- Estructura: detalle_pedido  (sin datos; se genera con endpoints)
-- --------------------------------------------------------
CREATE TABLE `detalle_pedido` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `pedido_id` bigint(20) NOT NULL,
  `producto_id` bigint(20) NOT NULL,
  `cantidad` int(11) NOT NULL,
  `precio_unitario` decimal(10,2) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_detalle_pedido` (`pedido_id`),
  KEY `fk_detalle_producto` (`producto_id`),
  CONSTRAINT `fk_detalle_pedido` FOREIGN KEY (`pedido_id`) REFERENCES `pedido` (`id`),
  CONSTRAINT `fk_detalle_producto` FOREIGN KEY (`producto_id`) REFERENCES `producto` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------
-- Estructura: pago  (SIN metodo)
-- --------------------------------------------------------
CREATE TABLE `pago` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `pedido_id` bigint(20) NOT NULL,
  `importe` decimal(10,2) NOT NULL,
  `fecha` date NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `pedido_id` (`pedido_id`),
  CONSTRAINT `fk_pago_pedido` FOREIGN KEY (`pedido_id`) REFERENCES `pedido` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------
-- Estructura: cotizacion  (estado SOLICITADA, enlace_whatsapp TEXT, pedido_id nullable)
-- --------------------------------------------------------
CREATE TABLE `cotizacion` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `dias_para_responder` int(11) NOT NULL,
  `enlace_whatsapp` text DEFAULT NULL,
  `estado` varchar(20) NOT NULL,
  `fecha_emision` date NOT NULL,
  `fecha_vencimiento` date NOT NULL,
  `monto_cotizado` decimal(10,2) NOT NULL,
  `pedido_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_cotizacion_pedido` (`pedido_id`),
  CONSTRAINT `FK_cotizacion_pedido` FOREIGN KEY (`pedido_id`) REFERENCES `pedido` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
