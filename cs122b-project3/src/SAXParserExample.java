import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.IOException;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class SAXParserExample extends DefaultHandler {

    List<Movie> myMovies;
    List<List<String>> myActors;
    Map<String, List<String>> actorsInMovies;
    private String tempVal;
    private Movie tempMovie;
    private String director;
    private String fid;
    private String FN;
    private String LN;
    private String SN;

    private MovieCategories mc;



    public SAXParserExample() {
        myMovies = new ArrayList<Movie>();
        actorsInMovies = new HashMap<>();
        myActors = new ArrayList<>();
    }

    public void run() {
        mc = new MovieCategories();
        parseDocument();
        printData();

    }

    private void parseDocument() {
        SAXParserFactory spf = SAXParserFactory.newInstance();

        try {
            SAXParser sp = spf.newSAXParser();
            sp.parse("/home/ubuntu/s23-122b-wjj/cs122b-project3/xml/mains243.xml", this);
            sp.parse("/home/ubuntu/s23-122b-wjj/cs122b-project3/xml/casts124.xml", this);
            sp.parse("/home/ubuntu/s23-122b-wjj/cs122b-project3/xml/actors63.xml", this);
        } catch (SAXException | ParserConfigurationException | IOException e) {
            e.printStackTrace();
        }
    }

    private void printData() {
        /*
        System.out.println("No of Movies '" + myMovies.size() + "'.");
        for (Movie movie : myMovies) {
            System.out.println(movie.toString());
        }*/
        //System.out.println(actorsInMovies);
        //System.out.println(myActors);
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        tempVal = "";
        if (qName.equalsIgnoreCase("film")) {
            tempMovie = new Movie();
        }

    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        tempVal = new String(ch, start, length);
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        if (qName.equalsIgnoreCase("dirname")) {
            director = tempVal;
        }
        if (qName.equalsIgnoreCase("film")) {
            myMovies.add(tempMovie);
            tempMovie.setDirectorName(director);
        } else if (qName.equalsIgnoreCase("t")) {
            tempMovie.setMovieTitle(tempVal);
        } else if (qName.equalsIgnoreCase("year")) {
            tempMovie.setMovieYear(tempVal);
        }
        else if (qName.equalsIgnoreCase("cat"))
        {
            tempMovie.addCategory(mc.getCategory(tempVal));
        }
        else if (qName.equalsIgnoreCase("fid"))
        {
            tempMovie.setMovieId(tempVal);
        }

        // for casts124
        else if (qName.equalsIgnoreCase("f")) {
            fid = tempVal;

        } else if(qName.equalsIgnoreCase("a"))
        {
            if (actorsInMovies.containsKey(fid))
            {
                List<String> actors = actorsInMovies.get(fid);
                actors.add(tempVal);
            } else {
                List<String> actors = new ArrayList<>();
                actors.add(tempVal);
                actorsInMovies.put(fid, actors);
            }
        }

        // for actors63
        else if (qName.equalsIgnoreCase("stagename")) {
            SN = tempVal;
        }
        else if (qName.equalsIgnoreCase("familyname")) {
            LN = tempVal;
        }
        else if (qName.equalsIgnoreCase("firstname")) {
            FN = tempVal;
        }
        else if (qName.equalsIgnoreCase("dob")) {
            ArrayList<String> temp = new ArrayList<String>();
            temp.add(SN);
            temp.add(FN + " " + LN);
            temp.add(tempVal);
            myActors.add(temp);
        }
    }


    public List<List<String>> getActors()
    {
        return myActors;
    }

    public Map<String, List<String>> getActorsInMoives()
    {
        return actorsInMovies;
    }

    public List<Movie> getMovies()
    {
        return myMovies;
    }

    public static void main(String[] args)
    {
        SAXParserExample spe = new SAXParserExample();
        spe.run();
        Insertion insertion = new Insertion();
        insertion.insertMovies(spe.getMovies());
        insertion.insertStars(spe.getActors());
        insertion.insertStarsInMovies(spe.getActorsInMoives());
        insertion.saveNotFoundDataToFile();
    }


}

