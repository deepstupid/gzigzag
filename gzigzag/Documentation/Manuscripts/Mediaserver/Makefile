all:
	latex mediaserver
	BIBINPUTS=..:$$BIBINPUTS bibtex mediaserver
	latex mediaserver
	dvips -f mediaserver.dvi -o mediaserver.ps
