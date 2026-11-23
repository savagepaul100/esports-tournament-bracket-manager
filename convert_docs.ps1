$word = New-Object -ComObject Word.Application
$word.Visible = $false
$docPath = (Get-Item "NexusArena_Project_Report.docx").FullName
$pdfPath = (Get-Item ".").FullName + "\NexusArena_Project_Report.pdf"
Write-Host "Converting $docPath to $pdfPath"
$doc = $word.Documents.Open($docPath)
$doc.SaveAs([ref]$pdfPath, [ref]17)
$doc.Close()
$word.Quit()
Write-Host "DOCX -> PDF successfully converted."

$ppt = New-Object -ComObject PowerPoint.Application
$presPath = (Get-Item "NexusArena_Presentation.pptx").FullName
$pptPdfPath = (Get-Item ".").FullName + "\NexusArena_Presentation.pdf"
Write-Host "Converting $presPath to $pptPdfPath"
$pres = $ppt.Presentations.Open($presPath, [Microsoft.Office.Core.MsoTriState]::msoFalse, [Microsoft.Office.Core.MsoTriState]::msoFalse, [Microsoft.Office.Core.MsoTriState]::msoFalse)
$pres.SaveAs($pptPdfPath, 32)
$pres.Close()
$ppt.Quit()
Write-Host "PPTX -> PDF successfully converted."
