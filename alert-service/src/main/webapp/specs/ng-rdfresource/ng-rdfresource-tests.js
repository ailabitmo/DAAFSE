var RDF_TYPE = "http://www.w3.org/1999/02/22-rdf-syntax-ns#type";

describe("Unit: Resource", function() {
    beforeEach(module('ngRDFResource', 'metersApp.utils'));
    
    var scope;
    
    beforeEach(inject(function($rootScope){
        scope = $rootScope;
    }));
    
    it("fromTriples: simple", inject(function(Resource) {
        var triples = [{
                subject: "http://example.com/resource/1",
                predicate: RDF_TYPE,
                object: "http://examplecom/ontology#Class"
        }];
    
        expect(Resource.prototype.fromTriples(triples)).toEqual(jasmine.objectContaining({
            'uri' : triples[0].subject,
            'rdf:type' : triples[0].object
        }));
        
    }));
    
    xit("fromTriples: multiple subjects", inject(function(utils, Resource) {
        var flag = false;
        
        runs(function() {
            var ttl = '<http://purl.org/daafse/observations/results/values/#voltage-mercury230_13534128-1404906263371-phase-1>\n\
                a   <http://purl.org/NET/ssnext/electricmeters#PolyphaseVoltageValue> ;\n\
                <http://purl.org/NET/ssnext/electricmeters#hasPhaseNumber>  1 ;\n\
                <http://purl.org/NET/ssnext/electricmeters#hasQuantityValue>    "232.16"^^<http://www.w3.org/2001/XMLSchema#double> .\n\
            <http://purl.org/daafse/observations/#voltage-mercury230_13534128-1404906263371>\n\
                a   <http://purl.org/NET/ssnext/electricmeters#PolyphaseVoltageObservation> ;\n\
                <http://purl.oclc.org/NET/ssnx/ssn#observationResult>   <http://purl.org/daafse/observations/results/#voltage-mercury230_13534128-1404906263371> ;\n\
                <http://purl.oclc.org/NET/ssnx/ssn#observationResultTime>   "1404906263371"^^<http://www.w3.org/2001/XMLSchema#long> ;\n\
                <http://purl.oclc.org/NET/ssnx/ssn#observedBy> <http://purl.org/daafse/meters/mercury230_13534128> .\n\
            <http://purl.org/daafse/observations/results/#voltage-mercury230_13534128-1404906263371>\n\
                a   <http://purl.org/NET/ssnext/electricmeters#PolyphaseVoltageSensorOutput> ;\n\
                <http://purl.oclc.org/NET/ssnx/ssn#hasValue>\n\
                    <http://purl.org/daafse/observations/results/values/#voltage-mercury230_13534128-1404906263371-phase-3> ,\n\
                    <http://purl.org/daafse/observations/results/values/#voltage-mercury230_13534128-1404906263371-phase-2> ,\n\
                    <http://purl.org/daafse/observations/results/values/#voltage-mercury230_13534128-1404906263371-phase-1> ;\n\
                <http://purl.oclc.org/NET/ssnx/ssn#isProducedBy> <http://purl.org/daafse/meters/mercury230_13534128> .\n\
            <http://purl.org/daafse/observations/results/values/#voltage-mercury230_13534128-1404906263371-phase-2>\n\
                a   <http://purl.org/NET/ssnext/electricmeters#PolyphaseVoltageValue> ;\n\
                <http://purl.org/NET/ssnext/electricmeters#hasPhaseNumber>  2 ;\n\
                <http://purl.org/NET/ssnext/electricmeters#hasQuantityValue>    "232.38"^^<http://www.w3.org/2001/XMLSchema#double> .\n\
            <http://purl.org/daafse/observations/results/values/#voltage-mercury230_13534128-1404906263371-phase-3>\n\
                a   <http://purl.org/NET/ssnext/electricmeters#PolyphaseVoltageValue> ;\n\
                <http://purl.org/NET/ssnext/electricmeters#hasPhaseNumber>  3 ;\n\
            <http://purl.org/NET/ssnext/electricmeters#hasQuantityValue>    "235.23"^^<http://www.w3.org/2001/XMLSchema#double> .';

            var thisArg = this;
            
            utils.parseTTL(ttl).then(function(triples) {
                thisArg.triples = triples;
                flag = true;
            });
        });
        
        waitsFor(function() {
            scope.$digest();
            return flag;
        }, 2000);
     
        runs(function() {
            console.log(Resource.prototype.fromTriples(this.triples));
            expect(this.triples).toBeDefined();
        });
    }));
});

describe("Unit: Graph", function() {
    beforeEach(module('ngRDFResource'));
    
    it("constructor: triples only", inject(function(Graph){
        var triples = [{
                subject: "http://example.com/resource/1",
                predicate: RDF_TYPE,
                object: "http://examplecom/ontology#Class"
        }];
        var graph = new Graph(triples);
        
        expect(graph.getByType("http://examplecom/ontology#Class")).toBeDefined();
    }));
});