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
		hypotenuse = (IMAGEWIDTH* TARGETHEIGHT)/(2 * blobHeight * FOV)
		arg = blobWidth / (blobHeight * (TARGETWIDTH/TARGETHEIGHT))
		tarAngle = ASIN(arg) * 180/3.14
		tarDistance = hypotenuse * Cos(tarAngle * 3.14/180)
		
		SetVariable "Hypotenuse", hypotenuse
		SetVariable "TarAngle", tarAngle
		
		SetVariable "Arg", arg
		SetVariable "TarDistance", tarDistance
		SetVariable "targeting", "Horizontal"
				
	end if
end if