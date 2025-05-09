
$option = $args[0]
$fileName = $args[1]


Start-Process "java" -ArgumentList "-jar", "Elf.jar", $option, $fileName -NoNewWindow -Wait

