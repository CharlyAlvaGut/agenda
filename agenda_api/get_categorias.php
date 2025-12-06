<?php

header("Content-Type: application/json; charset=utf-8");
require_once "config.php";

$sql = "SELECT pk_i_categoria, d_v_categoria 
        FROM c_categoria 
        WHERE w_v_status = 'A'
        ORDER BY pk_i_categoria";

$result = $conexion->query($sql);

$categorias = [];

if ($result) {
    while ($row = $result->fetch_assoc()) {
        $categorias[] = [
            "id" => (int)$row["pk_i_categoria"],
            "nombre" => $row["d_v_categoria"]
        ];
    }

    echo json_encode([
        "ok" => true,
        "data" => $categorias
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
