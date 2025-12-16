<?php
// agenda_api/get_agenda_home.php
date_default_timezone_set('America/Mexico_City'); // O tu zona horaria
header("Content-Type: application/json; charset=utf-8");
require_once "config.php";

$hoy = date("Y-m-d");

// ACTUALIZACIÓN: Agregamos LEFT JOIN con d_lugar y seleccionamos lat/long
$sql = "
    SELECT
        e.pk_i_evento,
        e.d_f_fechevento,
        e.d_t_horaevento,
        e.d_v_evento,
        s.d_v_estatus,
        l.c_n_latitud,
        l.c_n_longitud
    FROM m_evento e
    LEFT JOIN c_estatus s ON e.pk_i_estatus = s.pk_i_estatus
    LEFT JOIN d_lugar l   ON e.pk_i_evento  = l.pk_i_evento
    WHERE e.w_v_status = 'A'
      AND e.d_f_fechevento >= ?
    ORDER BY e.d_f_fechevento ASC, e.d_t_horaevento ASC
";

$stmt = $conexion->prepare($sql);
$stmt->bind_param("s", $hoy);
$stmt->execute();
$result = $stmt->get_result();

$eventos = [];
while ($row = $result->fetch_assoc()) {
    $eventos[] = [
        "id"      => (int)$row["pk_i_evento"],
        "fecha"   => $row["d_f_fechevento"],
        "hora"    => $row["d_t_horaevento"],
        "titulo"  => $row["d_v_evento"],
        "estatus" => $row["d_v_estatus"],
        // Agregamos latitud y longitud al JSON (si son nulos, enviamos 0)
        "latitud" => $row["c_n_latitud"] ? (double)$row["c_n_latitud"] : 0.0,
        "longitud"=> $row["c_n_longitud"]? (double)$row["c_n_longitud"]: 0.0
    ];
}

echo json_encode(["ok" => true, "data" => $eventos]);
?>