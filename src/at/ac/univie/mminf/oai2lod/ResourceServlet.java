package at.ac.univie.mminf.oai2lod;

import java.io.IOException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import de.fuberlin.wiwiss.pubby.negotiation.ContentTypeNegotiator;
import de.fuberlin.wiwiss.pubby.negotiation.MediaRangeSpec;
import de.fuberlin.wiwiss.pubby.negotiation.PubbyNegotiator;

public class ResourceServlet extends HttpServlet {

	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws IOException {
		String relativeResourceURI = request.getRequestURI().substring(
				request.getContextPath().length()
						+ request.getServletPath().length());
		// Some servlet containers keep the leading slash, some don't
		if (!"".equals(relativeResourceURI)
				&& "/".equals(relativeResourceURI.substring(0, 1))) {
			relativeResourceURI = relativeResourceURI.substring(1);
		}
		if (request.getQueryString() != null) {
			relativeResourceURI = relativeResourceURI + "?"
					+ request.getQueryString();
		}

		response.addHeader("Vary", "Accept, User-Agent");
		ContentTypeNegotiator negotiator = PubbyNegotiator.getPubbyNegotiator();
		MediaRangeSpec bestMatch = negotiator.getBestMatch(request
				.getHeader("Accept"), request.getHeader("User-Agent"));
		if (bestMatch == null) {
			response.setStatus(406);
			response.setContentType("text/plain");
			response.getOutputStream().println(
					"406 Not Acceptable: The requested data format is not supported. "
							+ "Only HTML and RDF are available.");
			return;
		}

		response.setStatus(303);
		response.setContentType("text/plain");
		OAI2LODServer server = OAI2LODServer
				.fromServletContext(getServletContext());
		String location;
		if ("text/html".equals(bestMatch.getMediaType())) {
			location = server.pageURL(relativeResourceURI);
		} else {
			location = server.dataURL(relativeResourceURI);
		}
		response.addHeader("Location", location);
		response.getOutputStream().println(
				"303 See Other: For a description of this item, see "
						+ location);
	}

	private static final long serialVersionUID = 2752377911405801794L;
}
