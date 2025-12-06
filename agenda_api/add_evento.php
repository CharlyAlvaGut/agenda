<?php
// add_evento.php
header("Content-Type: application/json; charset=utf-8");
require_once "config.php";

// Validar método
if ($_SERVER["REQUEST_METHOD"] !== "POST") {
    http_response_code(405);
    echo json_encode(["ok" => false, "error" => "Método no permitido"]);
    exit;
}

// Leer parámetros (pueden venir como form-data o x-www-form-urlencoded)
$fecha      = $_POST["fecha"]      ?? null; // formato: YYYY-MM-DD
$hora       = $_POST["hora"]       ?? null; // formato: HH:MM:SS
$evento     = $_POST["evento"]     ?? null; // descripción breve
$idCat      = $_POST["id_categoria"] ?? null;
$idEstatus  = $_POST["id_estatus"]   ?? null;

// Validación básica
if (!$fecha || !$hora || !$evento || !$idCat || !$idEstatus) {
    http_response_code(400);
    echo json_encode([
        "ok" => false,
        "error" => "Faltan parámetros"
    ]);
    exit;
}

// Usuario capturador y status
$usuario = "APP_MOVIL";  // puedes cambiarlo o recibirlo del app
$estatusRegistro = "A";

// Preparar INSERT
$stmt = $conexion->prepare("
    INSERT INTO m_evento 
    (d_f_fechevento, d_t_horaevento, d_v_evento, pk_i_categoria, pk_i_estatus, c_v_usucap, d_f_fechcap, w_v_status)
    VALUES (?, ?, ?, ?, ?, ?, NOW(), ?)
");

if (!$stmt) {
    http_response_code(500);
    echo json_encode(["ok" => false, "error" => $conexion->error]);
    exit;
}

$stmt->bind_param(
    "sssisss",
    $fecha,
    $hora,
    $evento,
    $idCat,
    $idEstatus,
    $usuario,
    $estatusRegistro
);

if ($stmt->execute()) {
    echo json_encode([
        "ok" => true,
        "id_evento" => $stmt->insert_id,
        "mensaje" => "Evento guardado correctamente"
    ]);
} else {
    http_response_code(500);
    echo json_encode([
        "ok" => false,
        "error" => $stmt->error
    ]);
}

$stmt->close();
$conexion->close();
?>
