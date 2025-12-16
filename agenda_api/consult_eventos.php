<?php
// consult_eventos.php
header("Content-Type: application/json; charset=utf-8");
require_once "config.php";

// Método esperado: GET (puedes cambiar a POST si quieres)
if ($_SERVER["REQUEST_METHOD"] !== "GET") {
    http_response_code(405);
    echo json_encode([
        "ok" => false,
        "error" => "Método no permitido, usa GET"
    ]);
    exit;
}

// Leer parámetros
$criterio    = $_GET["criterio"]      ?? null;          // fecha | rango | mes | anio
$idCategoria = isset($_GET["id_categoria"]) && $_GET["id_categoria"] !== ""
    ? intval($_GET["id_categoria"])
    : null;

// Validar criterio
$criterio = strtolower(trim((string)$criterio));
if (!in_array($criterio, ["fecha", "rango", "mes", "anio"])) {
    http_response_code(400);
    echo json_encode([
        "ok" => false,
        "error" => "Criterio inválido. Usa: fecha, rango, mes o anio"
    ]);
    exit;
}

// Base del SELECT
$selectBase = "
    SELECT 
        e.pk_i_evento         AS id_evento,
        e.d_f_fechevento      AS fecha,
        e.d_t_horaevento      AS hora,
        e.d_v_evento          AS evento,
        c.d_v_categoria       AS categoria,
        s.d_v_estatus         AS estatus,
        l.c_n_latitud         AS latitud,
        l.c_n_longitud        AS longitud
    FROM m_evento e
    LEFT JOIN c_categoria c ON e.pk_i_categoria = c.pk_i_categoria
    LEFT JOIN c_estatus   s ON e.pk_i_estatus   = s.pk_i_estatus
    LEFT JOIN d_lugar     l ON e.pk_i_evento    = l.pk_i_evento
    WHERE e.w_v_status = 'A'
";

// Variables para query final y tipos/valores de bind
$sql = "";
$tipos = "";
$valores = [];

// Armar SQL según criterio
if ($criterio === "fecha") {
    $fecha = $_GET["fecha"] ?? null;

    if (!$fecha) {
        http_response_code(400);
        echo json_encode([
            "ok" => false,
            "error" => "Falta parámetro 'fecha' (YYYY-MM-DD)"
        ]);
        exit;
    }

    $sql = $selectBase . " AND e.d_f_fechevento = ?";
    $tipos = "s";
    $valores[] = $fecha;

} elseif ($criterio === "rango") {
    $fechaInicio = $_GET["fecha_inicio"] ?? null;
    $fechaFin    = $_GET["fecha_fin"]    ?? null;

    if (!$fechaInicio || !$fechaFin) {
        http_response_code(400);
        echo json_encode([
            "ok" => false,
            "error" => "Faltan parámetros 'fecha_inicio' y/o 'fecha_fin' (YYYY-MM-DD)"
        ]);
        exit;
    }

    $sql = $selectBase . " AND e.d_f_fechevento BETWEEN ? AND ?";
    $tipos = "ss";
    $valores[] = $fechaInicio;
    $valores[] = $fechaFin;

} elseif ($criterio === "mes") {
    $mes  = isset($_GET["mes"])  ? intval($_GET["mes"])  : null;  // 1..12

        if (!$mes) {
            http_response_code(400);
            echo json_encode([
                "ok" => false,
                "error" => "Falta parámetro 'mes' (1-12)"
            ]);
            exit;
        }

        // Solo filtramos por mes. MySQL traerá Enero 2024, Enero 2025, etc.
        $sql = $selectBase . " AND MONTH(e.d_f_fechevento) = ?";
        $tipos = "i";
        $valores[] = $mes;

} elseif ($criterio === "anio") {
    $anio = isset($_GET["anio"]) ? intval($_GET["anio"]) : null;

    if (!$anio) {
        http_response_code(400);
        echo json_encode([
            "ok" => false,
            "error" => "Falta parámetro 'anio'"
        ]);
        exit;
    }

    $sql = $selectBase . " AND YEAR(e.d_f_fechevento) = ?";
    $tipos = "i";
    $valores[] = $anio;
}

// Si se envía id_categoria, agregar filtro extra
if (!is_null($idCategoria) && $idCategoria > 0) {
    $sql    .= " AND e.pk_i_categoria = ?";
    $tipos  .= "i";
    $valores[] = $idCategoria;
}

// Orden por fecha/hora
$sql .= " ORDER BY e.d_f_fechevento ASC, e.d_t_horaevento ASC";

// Preparar statement
$stmt = $conexion->prepare($sql);
if (!$stmt) {
    http_response_code(500);
    echo json_encode([
        "ok" => false,
        "error" => "Error al preparar la consulta: " . $conexion->error
    ]);
    exit;
}

// Bind dinámico
if ($tipos !== "" && count($valores) > 0) {
    $stmt->bind_param($tipos, ...$valores);
}

// Ejecutar
if (!$stmt->execute()) {
    http_response_code(500);
    echo json_encode([
        "ok" => false,
        "error" => "Error al ejecutar consulta: " . $stmt->error
    ]);
    $stmt->close();
    $conexion->close();
    exit;
}

// Obtener resultados
$result = $stmt->get_result();
$eventos = [];

while ($row = $result->fetch_assoc()) {
    $eventos[] = [
        "id_evento" => (int)$row["id_evento"],
        "fecha"     => $row["fecha"],
        "hora"      => $row["hora"],
        "evento"    => $row["evento"],
        "categoria" => $row["categoria"],
        "estatus"   => $row["estatus"],
        "latitud"   => $row["latitud"],
        "longitud"  => $row["longitud"]
    ];
}

$stmt->close();
$conexion->close();

// Respuesta final
echo json_encode([
    "ok"      => true,
    "criterio"=> $criterio,
    "total"   => count($eventos),
    "eventos" => $eventos
]);
?>
