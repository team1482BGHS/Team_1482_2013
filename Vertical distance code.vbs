blobCount = GetVariable("BLOB_COUNT")
blobHeight = GetFloatVariable("Blob_height")


	blobs = GetArrayVariable("blobs")
	distance = -1
IF blobHeight <> 0 then	
	distance = ((82 * 480) / (.8988 * blobHeight))
	SetVariable "distance", distance
 else
	
end if
