all: threads2.ent_sz

%.sorted: %.log
	sort -k2,2 < $< > $@

%.ent_sz: %.sorted
	perl add-entropy-size.perl < $< | sort -k2,2gr -k1,1gr > $@

