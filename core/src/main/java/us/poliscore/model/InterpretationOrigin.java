package us.poliscore.model;

import java.net.URL;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.SneakyThrows;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbIgnore;

@Data
@DynamoDbBean
@NoArgsConstructor
@AllArgsConstructor
public class InterpretationOrigin {
	public static final InterpretationOrigin POLISCORE = new InterpretationOrigin("https://poliscore.us", "PoliScore");
	
	@NonNull public String url;
	
	@NonNull public String title;
	
	@DynamoDbIgnore
	@JsonIgnore
	public String getIdHash() {
		try {
//			java.net.URI uri = java.net.URI.create(url);
//		    java.net.URL parsedUrl = uri.toURL();
			
			// Even though "new URL" here is deprecated, we actually don't have a real replacement for it here.
			// The recommended 'URI.create' solution is more strict in its validation which occasionally causes issues
			@SuppressWarnings("deprecation")
			URL parsedUrl = new URL(url);
			
		    String host = parsedUrl.getHost();

		    if (host == null || host.isEmpty()) {
		        String temp = url.replaceAll("^https?://", "");
		        int slashIndex = temp.indexOf('/');
		        host = (slashIndex == -1) ? temp : temp.substring(0, slashIndex);
		    }

		    if (host.startsWith("www.")) {
		        host = host.substring(4);
		    }

		    String cleaned = host.replaceAll("[^A-Za-z0-9]", "");
		    if (cleaned.length() > 6) {
		        cleaned = cleaned.substring(0, 6);
		    }

		    String path = parsedUrl.getPath();
		    if (path != null && !path.isEmpty() && !path.equals("/")) {
		        if (host.contains("reddit.com")) {
		            String[] parts = path.split("/");
		            for (int i = 0; i < parts.length - 1; i++) {
		                if (parts[i].equalsIgnoreCase("r") && !parts[i + 1].isEmpty()) {
		                    String subreddit = parts[i + 1].replaceAll("[^A-Za-z0-9]", "");
		                    if (subreddit.length() > 10) {
		                        subreddit = subreddit.substring(0, 10);
		                    }
		                    return "reddit/" + subreddit.toLowerCase();
		                }
		            }
		        }

		        String[] segments = path.split("/");
		        for (String seg : segments) {
		            if (!seg.isEmpty()) {
		                String readable = seg.replaceAll("[^A-Za-z0-9]", "");
		                if (readable.length() > 10) {
		                    readable = readable.substring(0, 10);
		                }
		                return (cleaned + "/" + readable).toLowerCase();
		            }
		        }
		    }

		    return cleaned.toLowerCase();

		} catch (Exception e) {
		    e.printStackTrace();
		    return "unknown";
		}

	}

	
	@Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof InterpretationOrigin)) return false;
        InterpretationOrigin that = (InterpretationOrigin) o;
        return this.getIdHash().equals(that.getIdHash());
    }

    @Override
    public int hashCode() {
        return getIdHash().hashCode();
    }
}