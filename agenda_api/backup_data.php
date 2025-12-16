<?php
// backup_data.php
header("Content-Type: application/json; charset=utf-8");
require_once "config.php";

// MODIFICACIÓN: Usamos LEFT JOIN para traer también la ubicación de d_lugar
$sql = "
    SELECT
        e.*,
        l.c_n_latitud,
        l.c_n_longitud
    FROM m_evento e
    LEFT JOIN d_lugar l ON e.pk_i_evento = l.pk_i_evento
    WHERE e.w_v_status = 'A'
";

$result = $conexion->query($sql);

$eventos = [];
while ($row = $result->fetch_assoc()) {
    $eventos[] = $row;
}

echo json_encode($eventos);
?>