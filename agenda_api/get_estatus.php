<?php

header("Content-Type: application/json; charset=utf-8");
require_once "config.php";

$sql = "SELECT pk_i_estatus, d_v_estatus 
        FROM c_estatus 
        WHERE w_v_status = 'A'
        ORDER BY pk_i_estatus";

$result = $conexion->query($sql);

$estatus = [];

if ($result) {
    while ($row = $result->fetch_assoc()) {
        $estatus[] = [
            "id" => (int)$row["pk_i_estatus"],
            "nombre" => $row["d_v_estatus"]
        ];
    }

    echo json_encode([
        "ok" => true,
        "data" => $estatus
    ]);
} else {
    http_response_code(500);
    echo json_encode([
        "ok" => false,
        "error" => $conexion->error
    ]);
}

$conexion->close();
?>
