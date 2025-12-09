$files = @(
    "D:\Proyectos\mvmsys\src\main\java\com\unam\mvmsys\controller\stock\ProductoController.java",
    "D:\Proyectos\mvmsys\src\main\java\com\unam\mvmsys\controller\comercial\ClienteController.java",
    "D:\Proyectos\mvmsys\src\main\java\com\unam\mvmsys\controller\comercial\ProveedorController.java"
)

foreach ($file in $files) {
    $content = Get-Content $file -Raw
    
    # Agregar padding y tamaño a los botones
    $content = $content -replace '(btn\.getStyleClass\(\)\.add\(esActivo \? "btn-pagination-active" : "btn-pagination"\);)', `
        '$1' + "`n        btn.setPrefSize(32, 32);`n        btn.setMinSize(32, 32);`n        btn.setPadding(Insets.EMPTY);"
    
    $content = $content -replace '(btn\.getStyleClass\(\)\.add\("btn-pagination-nav"\);)', `
        '$1' + "`n        btn.setPrefSize(32, 32);`n        btn.setMinSize(32, 32);`n        btn.setPadding(Insets.EMPTY);"
    
    Set-Content $file -Value $content
    Write-Host "Actualizado: $file"
}
