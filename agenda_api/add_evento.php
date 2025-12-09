<?php
// add_evento.php
header("Content-Type: application/json; charset=utf-8");
require_once "config.php";


if ($_SERVER["REQUEST_METHOD"] !== "POST") {
    http_response_code(405);
    echo json_encode(["ok" => false, "error" => "Método no permitido"]);
    exit;
}

$fecha      = $_POST["fecha"]        ?? null; 
$hora       = $_POST["hora"]         ?? null; 
$evento     = $_POST["evento"]       ?? null; 
$idCat      = $_POST["id_categoria"] ?? null;
$idEstatus  = $_POST["id_estatus"]   ?? null;
$latitud    = $_POST["latitud"]      ?? null;
$longitud   = $_POST["longitud"]     ?? null;


if (!$fecha || !$hora || !$evento || !$idCat || !$idEstatus) {
    http_response_code(400);
    echo json_encode([
        "ok" => false,
        "error" => "Faltan parámetros"
    ]);
    exit;
}

$usuario = "APP_MOVIL";   
$estatusRegistro = "A";

// INSERT en m_evento (maestro)
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
    $idEvento = $stmt->insert_id;

    // INSERT en d_lugar (detalle)
    $stmt2 = $conexion->prepare("
        INSERT INTO d_lugar 
        (pk_i_evento, c_n_longitud, c_n_latitud, c_v_usucap, d_f_fechcap, w_v_status)
        VALUES (?, ?, ?, ?, NOW(), ?)
    ");

    if (!$stmt2) {
        http_response_code(500);
        echo json_encode([
            "ok" => false,
            "error" => "Error al preparar d_lugar: " . $conexion->error
        ]);
        exit;
    }
    
    $stmt2->bind_param(
        "issss",
        $idEvento,
        $longitud,
        $latitud,
        $usuario,
        $estatusRegistro
    );

    if (!$stmt2->execute()) {
        http_response_code(500);
        echo json_encode([
            "ok" => false,
            "error" => "Error al insertar ubicación: " . $stmt2->error
        ]);
        exit;
    }

    $stmt2->close();

    echo json_encode([
        "ok" => true,
        "id_evento" => $idEvento,
        "mensaje" => "Evento y ubicación guardados correctamente"
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
