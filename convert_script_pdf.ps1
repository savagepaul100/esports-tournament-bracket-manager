$word = New-Object -ComObject Word.Application
$word.Visible = $false
$docPath = "C:\Users\PAUL\Downloads\Java Project\NexusArena_Presentation_Script_and_Roles.docx"
$pdfPath = "C:\Users\PAUL\Downloads\NexusArena_Presentation_Script_and_Roles.pdf"
Write-Host "Converting $docPath to $pdfPath"
$doc = $word.Documents.Open($docPath)
$doc.SaveAs([ref]$pdfPath, [ref]17)
$doc.Close()
$word.Quit()
Write-Host "PDF successfully saved to: $pdfPath"
