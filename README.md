# My own Apache Kafka Studies

Some Apache kKafka, Kafka streams , Kafka Connect, Strimzi, monitoring studies...

Better Read the [BOOK format](https://jbcodeforce.github.io/kafka-studies/).

## Building this booklet locally

The content of this repository is written with markdown files, packaged with [MkDocs](https://www.mkdocs.org/) and can be built into a book-readable format by MkDocs build processes.

1. Install MkDocs locally following the [official documentation instructions](https://www.mkdocs.org/#installation).
1. Install Material plugin for mkdocs:  `pip install mkdocs-material` 
1. clone this repo
1. `mkdocs serve`
1. Go to `http://127.0.0.1:8000/` in your browser.

### Pushing the book to GitHub Pages

1. Ensure that all your local changes to the `master` branch have been committed and pushed to the remote repository.
    `git push origin master`
1. Ensure that you have the latest commits to the `gh-pages` branch, so you can get others' updates.

	```bash
	git checkout gh-pages
	git pull origin gh-pages
	
	git checkout master
	```

1. Run `mkdocs gh-deploy` from the main directory.
