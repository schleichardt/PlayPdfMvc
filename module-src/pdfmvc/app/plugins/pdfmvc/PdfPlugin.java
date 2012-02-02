package plugins.pdfmvc;

import org.apache.fop.apps.FOPException;
import org.apache.fop.apps.Fop;
import org.apache.fop.apps.FopFactory;
import org.apache.fop.apps.MimeConstants;
import play.Logger;
import play.PlayPlugin;
import play.mvc.Http;

import javax.xml.transform.*;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamSource;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.lang.reflect.Method;

/** Intercepts HTTP requests for resources ending with .pdf to generate PDF files
 *
 * @author Michael Schleichardt
 */
public class PdfPlugin extends PlayPlugin {

    private static final String MIME_PDF = "application/pdf";
    private static final String PDF_FILE_ENDING = ".pdf";

    @Override
    public void beforeActionInvocation(Method actionMethod) {
        final Http.Request request = Http.Request.current();
        if (isRequestForDynamicPdf(request)) {
            request.format = "fo";//needed for templates
            final Http.Response response = Http.Response.current();
            request.contentType = MIME_PDF;
            response.contentType = MIME_PDF;
        }
        super.beforeActionInvocation(actionMethod);
    }

    @Override
    public void afterActionInvocation() {
        final Http.Response response = Http.Response.current();
        if (response.contentType.equals(MIME_PDF)) {
            ByteArrayOutputStream fopOutputStream = response.out;
            response.out = new ByteArrayOutputStream();
            FopFactory fopFactory = FopFactory.newInstance();
            try {
                Fop fop = fopFactory.newFop(MimeConstants.MIME_PDF, response.out);
                TransformerFactory factory = TransformerFactory.newInstance();
                Transformer transformer = factory.newTransformer();
                Source src = new StreamSource(new ByteArrayInputStream(fopOutputStream.toByteArray()));
                Result res = new SAXResult(fop.getDefaultHandler());
                transformer.transform(src, res);
                response.setHeader("Content-Length", response.out.size() + "");
            } catch (FOPException e) {
                throw new RuntimeException("can't render PDF");
            } catch (TransformerConfigurationException e) {
                throw new RuntimeException("can't render PDF");
            } catch (TransformerException e) {
                throw new RuntimeException("can't render PDF");
            }
        }
        super.afterActionInvocation();
    }

    @Override
    public void onApplicationStart() {
        Logger.info("Starting plugin " + PdfPlugin.class.getName());
        super.onApplicationStart();
    }

    private boolean isRequestForDynamicPdf(Http.Request request) {
        return request.path.toLowerCase().endsWith(PDF_FILE_ENDING);
    }
}
