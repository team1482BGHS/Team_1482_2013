blobCount = GetVariable("BLOB_COUNT")
blobHeight = GetFloatVariable("Blob_height")
blobWidth = GetFloatVariable("Blob_Width")

TARGETHEIGHT = 10.16
TARGETWIDTH = 59.7
IMAGEWIDTH = 640
IMAGEHEIGHT = 480
FOV = .64
if blobCount = 0 then
	
else
	if blobHeight > blobWidth then
	'Do the vertical distance
		if blobHeight <> 0 then	
			distance = ((82 * 480) / (.8988 * blobHeight))
			SetVariable "distance", distance
			SetVariable "targeting", "Vertical"
		end if
	else	'it is horizontal
		'hypotenuse = (307200)/( blobHeight * 1.28)
		'arg = blobWidth / (blobHeight * 5.87598)
		'tarAngle = ASIN(arg) * 57.3248
		'tarDistance = hypotenuse * Cos(tarAngle * .01744)
		
		'SetVariable "Hypotenuse", hypotenuse
		'SetVariable "TarAngle", tarAngle
		
		'SetVariable "Arg", arg
		'SetVariable "TarDistance", tarDistance
		'SetVariable "targeting", "Horizontal"
		'Simple version
	distance = ((82 * 640) /  blobWidth) '(2.252 * blobwidth
			SetVariable "distance", distance
			SetVariable "targeting", "Horizontal"
				
	end if
end if