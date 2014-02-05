blobCount = GetVariable("BLOB_COUNT")
blobHeight = GetFloatVariable("Blob_height")
blobWidth = GetFloatVariable("Blob_Width")

TARGETHEIGHT = 10.16
TARGETWIDTH = 59.7
IMAGEWIDTH = 640
IMAGEHEIGHT = 480
FOV = .64
hypotenuse = (IMAGEWIDTH* TARGETHEIGHT)/(2 * blobHeight * FOV)

arg = blobWidth / (blobHeight * (TARGETWIDTH/TARGETHEIGHT))
tarAngle = ASIN(arg) * 180/3.14
tarDistance = hypotenuse * Cos(tarAngle * 3.14/180)

SetVariable "Hypotenuse", hypotenuse
SetVariable "TarAngle", tarAngle

SetVariable "Arg", arg
SetVariable "TarDistance", tarDistance
