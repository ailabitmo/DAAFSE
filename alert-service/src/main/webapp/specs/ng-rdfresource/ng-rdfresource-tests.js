var RDF_TYPE = "http://www.w3.org/1999/02/22-rdf-syntax-ns#type";

describe("Unit: ResourceFactory", function() {
    beforeEach(module('ngRDFResource', 'metersApp.utils'));
    
    var scope;
    
    beforeEach(inject(function($rootScope){
        scope = $rootScope;
    }));
    
    it("newFromTriples: an empty triple set", inject(function(ResourceFactory) {
        var resource = ResourceFactory.newFromTriples([]);
        
        expect(resource).toBeNull();
    }));
    it("newFromTriples: a single rdf:type triple", inject(function(ResourceFactory) {
        var triples = [{
                subject: "http://example.com/resource/1",
                predicate: RDF_TYPE,
                object: "http://examplecom/ontology#Class"
        }];
    
        var resource = ResourceFactory.newFromTriples(triples);
    
        expect(resource).not.toBeNull();
        expect(resource).toEqual(jasmine.objectContaining({
            'uri' : triples[0].subject,
            'rdf:type' : [triples[0].object]
        }));
    }));
    xit("fromTriples: multiple subjects", inject(function(utils, ResourceFactory) {
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
            console.log(ResourceFactory.newFromTriples(this.triples));
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

describe("Unit: ResourceStore", function() {
    beforeEach(module('ngRDFResource'));
    
    it("genMissingTP: a prefixed name, not exists", inject(function(ResourceStore) {
        var patterns = ResourceStore.genMissingTP(
                'http://example.com/resource/1', ['rdf:type']);
        
        expect(patterns[0]).toEqual([
            '<http://example.com/resource/1>',
            'rdf:type',
            '?rdf_type'
        ]);
    }));  
    it("genMissingTP: a prefixed name, exists", inject(function(ResourceStore) {
        ResourceStore._store.addTriple(
                'http://example.com/resource/1', 
                'http://www.w3.org/1999/02/22-rdf-syntax-ns#type',
                'http://purl.org/NET/ssnext/electricmeters#Mercury230');

        var patterns = ResourceStore.genMissingTP(
                'http://example.com/resource/1', ['rdf:type']);
        
        expect(patterns).toEqual([]);
    }));
    it("genMissingTP: a path[2], all not exists", inject(function(ResourceStore) {
        var patterns = ResourceStore.genMissingTP(
                'http://example.com/resource/1', ['rdf:type', 'rdfs:label']);
        
        expect(patterns.length).toEqual(2);
        expect(patterns[0]).toEqual([
            '<http://example.com/resource/1>',
            'rdf:type',
            '?rdf_type'
        ]);
        expect(patterns[1]).toEqual([
            '?rdf_type',
            'rdfs:label',
            '?rdfs_label'
        ]);
    }));
    it("genMissingTP: a path[2], 1 - exists", inject(function(ResourceStore) {
        ResourceStore._store.addTriple(
                'http://example.com/resource/1', 
                'http://www.w3.org/1999/02/22-rdf-syntax-ns#type',
                'http://purl.org/NET/ssnext/electricmeters#Mercury230');
                
        var patterns = ResourceStore.genMissingTP(
                'http://example.com/resource/1', ['rdf:type', 'rdfs:label']);
                
        expect(patterns.length).toEqual(1);
        expect(patterns[0]).toEqual([
            '<http://purl.org/NET/ssnext/electricmeters#Mercury230>',
            'rdfs:label',
            '?rdfs_label'
        ]);
    }));
    it("genMissingTP: a path[2], all exists", inject(function(ResourceStore) {
        ResourceStore._store.addTriple(
                'http://example.com/resource/1', 
                'http://www.w3.org/1999/02/22-rdf-syntax-ns#type',
                'http://purl.org/NET/ssnext/electricmeters#Mercury230');
        ResourceStore._store.addTriple(
                'http://purl.org/NET/ssnext/electricmeters#Mercury230', 
                'http://www.w3.org/2000/01/rdf-schema#label',
                '\"Mercury 230\"@en');
                
        var patterns = ResourceStore.genMissingTP(
                'http://example.com/resource/1', ['rdf:type', 'rdfs:label']);
                
        expect(patterns).toEqual([]);
    }));
    it("get: a path[2], exists", inject(function(ResourceStore) {
        ResourceStore._store.addTriple(
                'http://example.com/resource/1', 
                'http://www.w3.org/1999/02/22-rdf-syntax-ns#type',
                'http://purl.org/NET/ssnext/electricmeters#Mercury230');
        ResourceStore._store.addTriple(
                'http://purl.org/NET/ssnext/electricmeters#Mercury230', 
                'http://www.w3.org/2000/01/rdf-schema#label',
                '\"Mercury 230\"@en');
        ResourceStore._store.addTriple(
                'http://purl.org/NET/ssnext/electricmeters#Mercury230', 
                'http://www.w3.org/2000/01/rdf-schema#label',
                '\"Меркурий 230\"@ru');
        
        var labels = ResourceStore.get(
                'http://example.com/resource/1', ['rdf:type', 'rdfs:label']);
        
        expect(labels.length).toEqual(2);
        expect(labels[0]).toEqual('\"Mercury 230\"@en');
        expect(labels[1]).toEqual('\"Меркурий 230\"@ru');
    }));
    it("getAndGenMissingTP: a prefixed name, not exists", inject(function (ResourceStore) {
        ResourceStore._store.addTriple(
                'http://example.com/resource/1', 
                'http://www.w3.org/1999/02/22-rdf-syntax-ns#type',
                'http://purl.org/NET/ssnext/electricmeters#Mercury230');
                
        var result = ResourceStore.getAndGenMissingTP(null, 'em:Mercury230', ['em:hasStream']);
        
        expect(result.map).toEqual(jasmine.objectContaining({
            'http://example.com/resource/1': {
                'rdf:type': ['http://purl.org/NET/ssnext/electricmeters#Mercury230']
            }
        }));
        expect(result.missing.length).toEqual(1);
        expect(result.missing[0]).toEqual(
                ['<http://example.com/resource/1>', 'em:hasStream', '?em_hasStream']);
    }));
    it("getAndGenMissingTP: two prefixed names, not exists", inject(function(ResourceStore) {
        ResourceStore._store.addTriple(
                'http://example.com/resource/1', 
                'http://www.w3.org/1999/02/22-rdf-syntax-ns#type',
                'http://purl.org/NET/ssnext/electricmeters#Mercury230');
                
        var result = ResourceStore.getAndGenMissingTP(null, 'em:Mercury230', 
                        ['em:hasStream', 'em:hasSerialNumber']);
        
        expect(result.map).toEqual(jasmine.objectContaining({
            'http://example.com/resource/1': {
                'rdf:type': ['http://purl.org/NET/ssnext/electricmeters#Mercury230']
            }
        }));
        expect(result.missing.length).toEqual(2);
        expect(result.missing[0]).toEqual(
                ['<http://example.com/resource/1>', 'em:hasStream', '?em_hasStream']);
        expect(result.missing[1]).toEqual(
                ['<http://example.com/resource/1>', 'em:hasSerialNumber', '?em_hasSerialNumber']);
    }));
    it("getAndGenMissingTP: a prefixed name and a path[2], all not exists", inject(function(ResourceStore) {
        var result = ResourceStore.getAndGenMissingTP(
                null, 'em:Mercury230', ['em:hasSerialNumber', 'rdf:type/rdfs:label']);
        
        expect(result.map).toEqual({});
        expect(result.missing.length).toEqual(4);
        expect(result.missing[0]).toEqual(['?uri', 'rdf:type', 'em:Mercury230']);
        expect(result.missing[1]).toEqual(
                ['?uri', 'em:hasSerialNumber', '?em_hasSerialNumber']);
        expect(result.missing[2]).toEqual(['?uri', 'rdf:type', '?rdf_type']);
        expect(result.missing[3]).toEqual(['?rdf_type', 'rdfs:label', '?rdfs_label']);
    }));
});

describe("Unit: Resource", function() {
    beforeEach(module('ngRDFResource'));
    
    it("get: a prefixed name, not exists", inject(function(ResourceFactory) {
        var triples = [{
                subject: "http://example.com/resource/1",
                predicate: RDF_TYPE,
                object: "http://examplecom/ontology#Class"
        }];
        
        var resource = ResourceFactory.newFromTriples(triples);
        
        var label = resource.get('rdfs:label');
        
        expect(label).toBeNull();
    }));
    it("get: a path[2], 1 - exists", inject(function(ResourceFactory) {
        var triples = [{
                subject: "http://example.com/resource/1",
                predicate: RDF_TYPE,
                object: "http://examplecom/ontology#Class"
        }];
        
        var resource = ResourceFactory.newFromTriples(triples);
        
        var label = resource.get('rdf:type/rdfs:label');

        expect(label).toBeNull();
    }));
    it("get: a path[2], all exist", inject(function(ResourceStore, ResourceFactory) {
        ResourceStore._store.addTriple(
                'http://example.com/resource/1', 
                RDF_TYPE,
                'http://purl.org/NET/ssnext/electricmeters#Mercury230');
        ResourceStore._store.addTriple(
                'http://purl.org/NET/ssnext/electricmeters#Mercury230', 
                'http://www.w3.org/2000/01/rdf-schema#label',
                '\"Mercury 230\"@en');
        ResourceStore._store.addTriple(
                'http://purl.org/NET/ssnext/electricmeters#Mercury230', 
                'http://www.w3.org/2000/01/rdf-schema#label',
                '\"Меркурий 230\"@ru');
                
        var triples = [{
                subject: 'http://example.com/resource/1',
                predicate: RDF_TYPE,
                object: 'http://purl.org/NET/ssnext/electricmeters#Mercury230'
        }];
        
        var resource = ResourceFactory.newFromTriples(triples);
        var label = resource.get('rdf:type/rdfs:label');
        
        expect(label).toEqual('\"Mercury 230\"@en');;
    }));
});