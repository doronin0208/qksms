<?php>
{
	$data = array("id" => "192.168.114.15", "check" => "1");

	header("Content-Type: application/json");
	echo json_encode($data);
	exit();
    
}
>