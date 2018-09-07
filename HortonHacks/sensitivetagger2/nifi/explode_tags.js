var parentFlowFile = session.get();
if (parentFlowFile !== null) {
    var InputStreamCallback = Java.type("org.apache.nifi.processor.io.InputStreamCallback");
    var OutputStreamCallback = Java.type("org.apache.nifi.processor.io.OutputStreamCallback");
    var IOUtils = Java.type("org.apache.commons.io.IOUtils");
    var StandardCharsets = Java.type("java.nio.charset.StandardCharsets");
    session.read(parentFlowFile, new InputStreamCallback(function(inputStream) {
	var inputText = IOUtils.toString(inputStream, StandardCharsets.UTF_8);
    var inputJson = JSON.parse(inputText);
	var tags = inputJson.tags;
    var hasTag = false;
    var splits = [];
    for (var i = 0; i < tags.length; i++) {
            var item = tags[i];
            var splitFlowFile = session.create(parentFlowFile);
            splitFlowFile = session.write(splitFlowFile, new OutputStreamCallback(function(outputStream) {
                outputStream.write(inputText.getBytes(StandardCharsets.UTF_8));
            }));
            splitFlowFile = session.putAllAttributes(splitFlowFile, {
                "tag": item
            });
            splits = splits.concat(splitFlowFile);
        }
        splits.forEach(function (splitFlowFile) {
            session.transfer(splitFlowFile, REL_SUCCESS);
        });
    }));
    session.remove(parentFlowFile);
}
