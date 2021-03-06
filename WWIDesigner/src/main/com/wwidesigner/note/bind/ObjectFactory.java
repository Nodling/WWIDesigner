//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.5 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2012.07.05 at 08:05:26 PM MDT 
//


package com.wwidesigner.note.bind;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the com.wwidesigner.note.bind package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {

    private final static QName _Tuning_QNAME = new QName("http://www.wwidesigner.com/Tuning", "tuning");
    private final static QName _FingeringPattern_QNAME = new QName("http://www.wwidesigner.com/Tuning", "fingeringPattern");
    private final static QName _Scale_QNAME = new QName("http://www.wwidesigner.com/Tuning", "scale");
    private final static QName _ScaleSymbolList_QNAME = new QName("http://www.wwidesigner.com/Tuning", "scaleSymbolList");
    private final static QName _Temperament_QNAME = new QName("http://www.wwidesigner.com/Tuning", "temperament");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: com.wwidesigner.note.bind
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link Scale }
     * 
     */
    public Scale createScale() {
        return new Scale();
    }

    /**
     * Create an instance of {@link ScaleSymbolList }
     * 
     */
    public ScaleSymbolList createScaleSymbolList() {
        return new ScaleSymbolList();
    }

    /**
     * Create an instance of {@link Temperament }
     * 
     */
    public Temperament createTemperament() {
        return new Temperament();
    }

    /**
     * Create an instance of {@link Tuning }
     * 
     */
    public Tuning createTuning() {
        return new Tuning();
    }

    /**
     * Create an instance of {@link FingeringPattern }
     * 
     */
    public FingeringPattern createFingeringPattern() {
        return new FingeringPattern();
    }

    /**
     * Create an instance of {@link Fingering }
     * 
     */
    public Fingering createFingering() {
        return new Fingering();
    }

    /**
     * Create an instance of {@link com.wwidesigner.note.bind.Note }
     * 
     */
    public com.wwidesigner.note.bind.Note createNote() {
        return new com.wwidesigner.note.bind.Note();
    }

    /**
     * Create an instance of {@link Scale.Note }
     * 
     */
    public Scale.Note createScaleNote() {
        return new Scale.Note();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Tuning }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.wwidesigner.com/Tuning", name = "tuning")
    public JAXBElement<Tuning> createTuning(Tuning value) {
        return new JAXBElement<Tuning>(_Tuning_QNAME, Tuning.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link FingeringPattern }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.wwidesigner.com/Tuning", name = "fingeringPattern")
    public JAXBElement<FingeringPattern> createFingeringPattern(FingeringPattern value) {
        return new JAXBElement<FingeringPattern>(_FingeringPattern_QNAME, FingeringPattern.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Scale }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.wwidesigner.com/Tuning", name = "scale")
    public JAXBElement<Scale> createScale(Scale value) {
        return new JAXBElement<Scale>(_Scale_QNAME, Scale.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ScaleSymbolList }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.wwidesigner.com/Tuning", name = "scaleSymbolList")
    public JAXBElement<ScaleSymbolList> createScaleSymbolList(ScaleSymbolList value) {
        return new JAXBElement<ScaleSymbolList>(_ScaleSymbolList_QNAME, ScaleSymbolList.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Temperament }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.wwidesigner.com/Tuning", name = "temperament")
    public JAXBElement<Temperament> createTemperament(Temperament value) {
        return new JAXBElement<Temperament>(_Temperament_QNAME, Temperament.class, null, value);
    }

}
