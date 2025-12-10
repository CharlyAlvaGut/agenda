<?php
// delete_evento.php
header("Content-Type: application/json; charset=utf-8");
require_once "config.php";

// 1. Validar método
if ($_SERVER["REQUEST_METHOD"] !== "POST") {
    http_response_code(405);
    echo json_encode([
        "ok"    => false,
        "error" => "Método no permitido. Usa POST."
    ]);
    exit;
}

// 2. Leer parámetro
$idEvento = $_POST["id_evento"] ?? null;

if (!$idEvento || !ctype_digit($idEvento)) {
    http_response_code(400);
    echo json_encode([
        "ok"    => false,
        "error" => "Parámetro id_evento inválido o faltante."
    ]);
    exit;
}

$idEvento = (int)$idEvento;

try {
    // 3. Iniciar transacción
    if (!$conexion->begin_transaction()) {
        throw new Exception("No se pudo iniciar la transacción: " . $conexion->error);
    }

    // 4. Borrar detalle primero (d_lugar)
    $stmtDetalle = $conexion->prepare("
        DELETE FROM d_lugar
        WHERE pk_i_evento = ?
    ");

    if (!$stmtDetalle) {
        throw new Exception("Error al preparar borrado de d_lugar: " . $conexion->error);
    }

    $stmtDetalle->bind_param("i", $idEvento);

    if (!$stmtDetalle->execute()) {
        throw new Exception("Error al ejecutar borrado en d_lugar: " . $stmtDetalle->error);
    }

    $stmtDetalle->close();

    // 5. Borrar maestro (m_evento)
    $stmtMaestro = $conexion->prepare("
        DELETE FROM m_evento
        WHERE pk_i_evento = ?
    ");

    if (!$stmtMaestro) {
        throw new Exception("Error al preparar borrado de m_evento: " . $conexion->error);
    }

    $stmtMaestro->bind_param("i", $idEvento);

    if (!$stmtMaestro->execute()) {
        throw new Exception("Error al ejecutar borrado en m_evento: " . $stmtMaestro->error);
    }

    // Verificar si realmente se borró algún registro en m_evento
    if ($stmtMaestro->affected_rows === 0) {
        // No existía el evento
        $conexion->rollback();
        http_response_code(404);
        echo json_encode([
            "ok"    => false,
            "error" => "No se encontró el evento con id_evento = $idEvento."
        ]);
        $stmtMaestro->close();
        $conexion->close();
        exit;
    }

    $stmtMaestro->close();

    // 6. Confirmar transacción
    if (!$conexion->commit()) {
        throw new Exception("Error al hacer commit: " . $conexion->error);
    }

    echo json_encode([
        "ok"        => true,
        "id_evento" => $idEvento,
        "mensaje"   => "Evento eliminado correctamente."
    ]);

} catch (Exception $e) {
    // Revertir si hay error
    if ($conexion->errno === 0) {
        // Si no hay error de conexión pero algo lógico falló
        $conexion->rollback();
    }

    http_response_code(500);
    echo json_encode([
        "ok"    => false,
        "error" => $e->getMessage()
    ]);
}

// 7. Cerrar conexión
$conexion->close();
