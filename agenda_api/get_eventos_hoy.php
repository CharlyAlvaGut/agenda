<?php
// get_eventos_hoy.php
header("Content-Type: application/json; charset=utf-8");
require_once "config.php";

// Hoy en formato YYYY-MM-DD (lado servidor)
$hoy = date("Y-m-d");

$sql = "
    SELECT 
        e.pk_i_evento,
        e.d_f_fechevento,
        e.d_t_horaevento,
        e.d_v_evento,
        c.d_v_categoria,
        s.d_v_estatus
    FROM m_evento e
    LEFT JOIN c_categoria c ON e.pk_i_categoria = c.pk_i_categoria
    LEFT JOIN c_estatus s   ON e.pk_i_estatus   = s.pk_i_estatus
    WHERE e.w_v_status = 'A'
      AND e.d_f_fechevento = ?
    ORDER BY e.d_t_horaevento
";

$stmt = $conexion->prepare($sql);
$stmt->bind_param("s", $hoy);
$stmt->execute();
$result = $stmt->get_result();

$eventos = [];

while ($row = $result->fetch_assoc()) {
    $eventos[] = [
        "id"        => (int)$row["pk_i_evento"],
        "fecha"     => $row["d_f_fechevento"],
        "hora"      => $row["d_t_horaevento"],
        "evento"    => $row["d_v_evento"],
        "categoria" => $row["d_v_categoria"],
        "estatus"   => $row["d_v_estatus"]
    ];
}

echo json_encode([
    "ok" => true,
    "data" => $eventos
]);

$stmt->close();
$conexion->close();
?>
