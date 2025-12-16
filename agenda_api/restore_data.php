<?php
// restore_data.php
header("Content-Type: application/json; charset=utf-8");
require_once "config.php";

$json = file_get_contents('php://input');
$datos = json_decode($json, true);

if (!$datos) {
    echo json_encode(["ok" => false, "error" => "JSON inválido"]);
    exit;
}

// Preparamos la inserción del EVENTO PRINCIPAL
$stmtEvento = $conexion->prepare("INSERT INTO m_evento (d_f_fechevento, d_t_horaevento, d_v_evento, pk_i_categoria, pk_i_estatus, c_v_usucap, d_f_fechcap, w_v_status) VALUES (?, ?, ?, ?, ?, ?, ?, ?)");

// Preparamos la inserción del LUGAR (Ubicación)
$stmtLugar = $conexion->prepare("INSERT INTO d_lugar (pk_i_evento, c_n_latitud, c_n_longitud, c_v_usucap, d_f_fechcap, w_v_status) VALUES (?, ?, ?, ?, ?, ?)");

$contador = 0;

foreach ($datos as $fila) {
    // 1. Insertar el Evento
    $fecha   = $fila['d_f_fechevento'] ?? date('Y-m-d');
    $hora    = $fila['d_t_horaevento'] ?? '00:00:00';
    $evento  = $fila['d_v_evento']     ?? 'Evento restaurado';
    $cat     = $fila['pk_i_categoria'] ?? 1;
    $estatus = $fila['pk_i_estatus']   ?? 2;
    $usu     = $fila['c_v_usucap']     ?? 'RESTORE';
    $fechcap = $fila['d_f_fechcap']    ?? date('Y-m-d H:i:s');
    $status  = $fila['w_v_status']     ?? 'A';

    $stmtEvento->bind_param("sssiisss", $fecha, $hora, $evento, $cat, $estatus, $usu, $fechcap, $status);

    if ($stmtEvento->execute()) {
        $contador++;

        // Obtenemos el ID del evento recién creado
        $nuevoIdEvento = $conexion->insert_id;

        // 2. Verificar si el respaldo tenía ubicación
        if (!empty($fila['c_n_latitud']) && !empty($fila['c_n_longitud'])) {
            $lat = $fila['c_n_latitud'];
            $lon = $fila['c_n_longitud'];

            // Insertamos en d_lugar vinculando con el nuevo ID
            $stmtLugar->bind_param("isssss", $nuevoIdEvento, $lat, $lon, $usu, $fechcap, $status);
            $stmtLugar->execute();
        }
    }
}

echo json_encode(["ok" => true, "insertados" => $contador]);
?>