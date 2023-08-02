const langs = ['ru', 'en']

let language = (navigator.language || navigator.browserLanguage)
                    .substring(0, 2)

const pathname = window.location.pathname
if (!langs.includes(language)) language = 'en'

const langSpecified = /^(\/..\/)/.test(pathname)

if (!langSpecified) window.location.pathname = `/${language}` + pathname

