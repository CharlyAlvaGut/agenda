<?php

$host = "localhost";
$port = "3306";              
$user = "root";              
$password = "root";
$dbname = "db_agenda";

// Conexión usando mysqli
$conexion = new mysqli($host . ":" . $port, $user, $password, $dbname);

// Verificar conexión
if ($conexion->connect_errno) {
    http_response_code(500);
    die(json_encode([
        "ok" => false,
        "error" => "Error de conexión: " . $conexion->connect_error
    ]));
}

// Forzar UTF-8
$conexion->set_charset("utf8");
?>
