var reader;
var curPage = 1;
var maxPage = 1;
var curPercentage = 0.0;
Monocle.DEBUG = true;

// Initialize the reader element.
Monocle.Events.listen(window, 'load', function () {
	window.reader = Monocle.Reader('reader', null, {
	flipper : Monocle.Flippers.Instant
	});

});

function getPage()
{
	return reader.getPlace().getLocus().page;
}

function getPercentage()
{
	return reader.getPlace().getLocus().percent;
}

function getTotalPageNum() {

	reader.moveTo( {
		percent : 1.0
	});
	maxPage = getPage();
	window.android.setTotalPageNum(maxPage);

	reader.moveTo( {
		page : 1
	});
}

/**
 * Opens by page percentage
 * 
 * @param percentage
 */
function openPageByPercentage(percentage) {

	reader.moveTo( {
		percent : percentage
	});
	curPercentage = percentage;
	curPage = getPage();
	window.android.setCurPageLocation(curPage, curPercentage);
}

/**
 * Opens previous page
 */
function prevPage() {
	reader.moveTo( {
		direction : -1
	});
	curPage = getPage();
	curPercentage = getPercentage();
	setTimeout("window.android.setCurPageLocation(curPage, curPercentage)", 1);
}

/**
 * Opens next page
 */
function nextPage() {
	reader.moveTo( {
		direction : 1
	});
	curPage = getPage();
	curPercentage = getPercentage();
	setTimeout("window.android.setCurPageLocation(curPage, curPercentage)", 1);
}