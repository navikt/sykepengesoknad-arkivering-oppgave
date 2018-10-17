package no.nav.syfo;

public class TestUtils {

    public static String soknadSelvstendigMangeSvar = "{\n" +
            "        \"id\": \"cf1e4a4c-5d94-4a00-8491-7b20afc89558\",\n" +
            "        \"aktorId\": \"aktorId\",\n" +
            "        \"sykmeldingId\": \"109c6792-3280-42ee-b582-2dfc83ceeed9\",\n" +
            "        \"soknadstype\": \"SELVSTENDIGE_OG_FRILANSERE\",\n" +
            "        \"status\": \"SENDT\",\n" +
            "        \"fom\": \"2018-09-28\",\n" +
            "        \"tom\": \"2018-10-15\",\n" +
            "        \"opprettetDato\": \"2018-10-16\",\n" +
            "        \"innsendtDato\": \"2018-10-16\",\n" +
            "        \"korrigerer\": null,\n" +
            "        \"korrigertAv\": null,\n" +
            "        \"sporsmal\": [\n" +
            "            {\n" +
            "                \"id\": \"22960\",\n" +
            "                \"tag\": \"ANSVARSERKLARING\",\n" +
            "                \"sporsmalstekst\": \"Jeg vet at dersom jeg gir uriktige opplysninger, eller holder tilbake opplysninger som har betydning for min rett til sykepenger, kan pengene holdes tilbake eller kreves tilbake, og/eller det kan medføre straffeansvar. Jeg er også klar over at jeg må melde fra til NAV dersom jeg i sykmeldingsperioden satt i varetekt, sonet straff eller var under forvaring.\",\n" +
            "                \"undertekst\": null,\n" +
            "                \"svartype\": \"CHECKBOX\",\n" +
            "                \"min\": null,\n" +
            "                \"max\": null,\n" +
            "                \"kriterieForVisningAvUndersporsmal\": null,\n" +
            "                \"svar\": [\n" +
            "                    {\n" +
            "                        \"verdi\": \"CHECKED\"\n" +
            "                    }\n" +
            "                ],\n" +
            "                \"undersporsmal\": []\n" +
            "            },\n" +
            "            {\n" +
            "                \"id\": \"22961\",\n" +
            "                \"tag\": \"TILBAKE_I_ARBEID\",\n" +
            "                \"sporsmalstekst\": \"Var du tilbake i fullt arbeid som frilanser før sykmeldingsperioden utløp 15.10.2018?\",\n" +
            "                \"undertekst\": null,\n" +
            "                \"svartype\": \"JA_NEI\",\n" +
            "                \"min\": null,\n" +
            "                \"max\": null,\n" +
            "                \"kriterieForVisningAvUndersporsmal\": \"JA\",\n" +
            "                \"svar\": [\n" +
            "                    {\n" +
            "                        \"verdi\": \"JA\"\n" +
            "                    }\n" +
            "                ],\n" +
            "                \"undersporsmal\": [\n" +
            "                    {\n" +
            "                        \"id\": \"22962\",\n" +
            "                        \"tag\": \"TILBAKE_NAR\",\n" +
            "                        \"sporsmalstekst\": \"Når var du tilbake i arbeid?\",\n" +
            "                        \"undertekst\": null,\n" +
            "                        \"svartype\": \"DATO\",\n" +
            "                        \"min\": \"2018-09-28\",\n" +
            "                        \"max\": \"2018-10-16\",\n" +
            "                        \"kriterieForVisningAvUndersporsmal\": null,\n" +
            "                        \"svar\": [\n" +
            "                            {\n" +
            "                                \"verdi\": \"2018-10-11\"\n" +
            "                            }\n" +
            "                        ],\n" +
            "                        \"undersporsmal\": []\n" +
            "                    }\n" +
            "                ]\n" +
            "            },\n" +
            "            {\n" +
            "                \"id\": \"22963\",\n" +
            "                \"tag\": \"JOBBET_DU_GRADERT_0\",\n" +
            "                \"sporsmalstekst\": \"I perioden 28.09.2018 - 06.10.2018 skulle du ifølge sykmeldingen jobbe 60% som frilanser. Jobbet du mer enn dette?\",\n" +
            "                \"undertekst\": null,\n" +
            "                \"svartype\": \"JA_NEI\",\n" +
            "                \"min\": null,\n" +
            "                \"max\": null,\n" +
            "                \"kriterieForVisningAvUndersporsmal\": \"JA\",\n" +
            "                \"svar\": [\n" +
            "                    {\n" +
            "                        \"verdi\": \"NEI\"\n" +
            "                    }\n" +
            "                ],\n" +
            "                \"undersporsmal\": [\n" +
            "                    {\n" +
            "                        \"id\": \"22964\",\n" +
            "                        \"tag\": \"HVOR_MANGE_TIMER_0\",\n" +
            "                        \"sporsmalstekst\": \"Hvor mange timer jobber du normalt per uke som frilanser?\",\n" +
            "                        \"undertekst\": null,\n" +
            "                        \"svartype\": \"TIMER\",\n" +
            "                        \"min\": \"1\",\n" +
            "                        \"max\": \"150\",\n" +
            "                        \"kriterieForVisningAvUndersporsmal\": null,\n" +
            "                        \"svar\": [],\n" +
            "                        \"undersporsmal\": []\n" +
            "                    },\n" +
            "                    {\n" +
            "                        \"id\": \"22965\",\n" +
            "                        \"tag\": \"HVOR_MYE_HAR_DU_JOBBET_0\",\n" +
            "                        \"sporsmalstekst\": \"Hvor mye jobbet du totalt i denne perioden som frilanser?\",\n" +
            "                        \"undertekst\": null,\n" +
            "                        \"svartype\": \"PROSENT\",\n" +
            "                        \"min\": \"61\",\n" +
            "                        \"max\": \"99\",\n" +
            "                        \"kriterieForVisningAvUndersporsmal\": null,\n" +
            "                        \"svar\": [],\n" +
            "                        \"undersporsmal\": []\n" +
            "                    }\n" +
            "                ]\n" +
            "            },\n" +
            "            {\n" +
            "                \"id\": \"22966\",\n" +
            "                \"tag\": \"JOBBET_DU_100_PROSENT_1\",\n" +
            "                \"sporsmalstekst\": \"I perioden 07.10.2018 - 15.10.2018 var du 100% sykmeldt som frilanser. Jobbet du noe i denne perioden?\",\n" +
            "                \"undertekst\": null,\n" +
            "                \"svartype\": \"JA_NEI\",\n" +
            "                \"min\": null,\n" +
            "                \"max\": null,\n" +
            "                \"kriterieForVisningAvUndersporsmal\": \"JA\",\n" +
            "                \"svar\": [\n" +
            "                    {\n" +
            "                        \"verdi\": \"JA\"\n" +
            "                    }\n" +
            "                ],\n" +
            "                \"undersporsmal\": [\n" +
            "                    {\n" +
            "                        \"id\": \"22967\",\n" +
            "                        \"tag\": \"HVOR_MANGE_TIMER_1\",\n" +
            "                        \"sporsmalstekst\": \"Hvor mange timer jobber du normalt per uke som frilanser?\",\n" +
            "                        \"undertekst\": null,\n" +
            "                        \"svartype\": \"TIMER\",\n" +
            "                        \"min\": \"1\",\n" +
            "                        \"max\": \"150\",\n" +
            "                        \"kriterieForVisningAvUndersporsmal\": null,\n" +
            "                        \"svar\": [\n" +
            "                            {\n" +
            "                                \"verdi\": \"40\"\n" +
            "                            }\n" +
            "                        ],\n" +
            "                        \"undersporsmal\": []\n" +
            "                    },\n" +
            "                    {\n" +
            "                        \"id\": \"22968\",\n" +
            "                        \"tag\": \"HVOR_MYE_HAR_DU_JOBBET_1\",\n" +
            "                        \"sporsmalstekst\": \"Hvor mye jobbet du totalt i denne perioden som frilanser?\",\n" +
            "                        \"undertekst\": null,\n" +
            "                        \"svartype\": \"PROSENT\",\n" +
            "                        \"min\": \"1\",\n" +
            "                        \"max\": \"99\",\n" +
            "                        \"kriterieForVisningAvUndersporsmal\": null,\n" +
            "                        \"svar\": [\n" +
            "                            {\n" +
            "                                \"verdi\": \"54\"\n" +
            "                            }\n" +
            "                        ],\n" +
            "                        \"undersporsmal\": []\n" +
            "                    }\n" +
            "                ]\n" +
            "            },\n" +
            "            {\n" +
            "                \"id\": \"22969\",\n" +
            "                \"tag\": \"ANDRE_INNTEKTSKILDER\",\n" +
            "                \"sporsmalstekst\": \"Har du andre inntektskilder eller arbeidsforhold?\",\n" +
            "                \"undertekst\": null,\n" +
            "                \"svartype\": \"JA_NEI\",\n" +
            "                \"min\": null,\n" +
            "                \"max\": null,\n" +
            "                \"kriterieForVisningAvUndersporsmal\": \"JA\",\n" +
            "                \"svar\": [\n" +
            "                    {\n" +
            "                        \"verdi\": \"JA\"\n" +
            "                    }\n" +
            "                ],\n" +
            "                \"undersporsmal\": [\n" +
            "                    {\n" +
            "                        \"id\": \"22970\",\n" +
            "                        \"tag\": \"HVILKE_ANDRE_INNTEKTSKILDER\",\n" +
            "                        \"sporsmalstekst\": \"Hvilke andre inntektskilder har du?\",\n" +
            "                        \"undertekst\": \"Du trenger ikke oppgi andre ytelser fra NAV\",\n" +
            "                        \"svartype\": \"CHECKBOX_GRUPPE\",\n" +
            "                        \"min\": null,\n" +
            "                        \"max\": null,\n" +
            "                        \"kriterieForVisningAvUndersporsmal\": null,\n" +
            "                        \"svar\": [],\n" +
            "                        \"undersporsmal\": [\n" +
            "                            {\n" +
            "                                \"id\": \"22971\",\n" +
            "                                \"tag\": \"INNTEKTSKILDE_ARBEIDSFORHOLD\",\n" +
            "                                \"sporsmalstekst\": \"Arbeidsforhold\",\n" +
            "                                \"undertekst\": null,\n" +
            "                                \"svartype\": \"CHECKBOX\",\n" +
            "                                \"min\": null,\n" +
            "                                \"max\": null,\n" +
            "                                \"kriterieForVisningAvUndersporsmal\": \"CHECKED\",\n" +
            "                                \"svar\": [],\n" +
            "                                \"undersporsmal\": [\n" +
            "                                    {\n" +
            "                                        \"id\": \"22972\",\n" +
            "                                        \"tag\": \"INNTEKTSKILDE_ARBEIDSFORHOLD_ER_DU_SYKMELDT\",\n" +
            "                                        \"sporsmalstekst\": \"Er du sykmeldt fra dette?\",\n" +
            "                                        \"undertekst\": null,\n" +
            "                                        \"svartype\": \"JA_NEI\",\n" +
            "                                        \"min\": null,\n" +
            "                                        \"max\": null,\n" +
            "                                        \"kriterieForVisningAvUndersporsmal\": null,\n" +
            "                                        \"svar\": [],\n" +
            "                                        \"undersporsmal\": []\n" +
            "                                    }\n" +
            "                                ]\n" +
            "                            },\n" +
            "                            {\n" +
            "                                \"id\": \"22973\",\n" +
            "                                \"tag\": \"INNTEKTSKILDE_JORDBRUKER\",\n" +
            "                                \"sporsmalstekst\": \"Jordbruker / Fisker / Reindriftsutøver\",\n" +
            "                                \"undertekst\": null,\n" +
            "                                \"svartype\": \"CHECKBOX\",\n" +
            "                                \"min\": null,\n" +
            "                                \"max\": null,\n" +
            "                                \"kriterieForVisningAvUndersporsmal\": \"CHECKED\",\n" +
            "                                \"svar\": [\n" +
            "                                    {\n" +
            "                                        \"verdi\": \"CHECKED\"\n" +
            "                                    }\n" +
            "                                ],\n" +
            "                                \"undersporsmal\": [\n" +
            "                                    {\n" +
            "                                        \"id\": \"22974\",\n" +
            "                                        \"tag\": \"INNTEKTSKILDE_JORDBRUKER_ER_DU_SYKMELDT\",\n" +
            "                                        \"sporsmalstekst\": \"Er du sykmeldt fra dette?\",\n" +
            "                                        \"undertekst\": null,\n" +
            "                                        \"svartype\": \"JA_NEI\",\n" +
            "                                        \"min\": null,\n" +
            "                                        \"max\": null,\n" +
            "                                        \"kriterieForVisningAvUndersporsmal\": null,\n" +
            "                                        \"svar\": [\n" +
            "                                            {\n" +
            "                                                \"verdi\": \"JA\"\n" +
            "                                            }\n" +
            "                                        ],\n" +
            "                                        \"undersporsmal\": []\n" +
            "                                    }\n" +
            "                                ]\n" +
            "                            },\n" +
            "                            {\n" +
            "                                \"id\": \"22975\",\n" +
            "                                \"tag\": \"INNTEKTSKILDE_FRILANSER_SELVSTENDIG\",\n" +
            "                                \"sporsmalstekst\": \"Selvstendig næringsdrivende\",\n" +
            "                                \"undertekst\": null,\n" +
            "                                \"svartype\": \"CHECKBOX\",\n" +
            "                                \"min\": null,\n" +
            "                                \"max\": null,\n" +
            "                                \"kriterieForVisningAvUndersporsmal\": \"CHECKED\",\n" +
            "                                \"svar\": [\n" +
            "                                    {\n" +
            "                                        \"verdi\": \"CHECKED\"\n" +
            "                                    }\n" +
            "                                ],\n" +
            "                                \"undersporsmal\": [\n" +
            "                                    {\n" +
            "                                        \"id\": \"22976\",\n" +
            "                                        \"tag\": \"INNTEKTSKILDE_FRILANSER_SELVSTENDIG_ER_DU_SYKMELDT\",\n" +
            "                                        \"sporsmalstekst\": \"Er du sykmeldt fra dette?\",\n" +
            "                                        \"undertekst\": null,\n" +
            "                                        \"svartype\": \"JA_NEI\",\n" +
            "                                        \"min\": null,\n" +
            "                                        \"max\": null,\n" +
            "                                        \"kriterieForVisningAvUndersporsmal\": null,\n" +
            "                                        \"svar\": [\n" +
            "                                            {\n" +
            "                                                \"verdi\": \"NEI\"\n" +
            "                                            }\n" +
            "                                        ],\n" +
            "                                        \"undersporsmal\": []\n" +
            "                                    }\n" +
            "                                ]\n" +
            "                            },\n" +
            "                            {\n" +
            "                                \"id\": \"22977\",\n" +
            "                                \"tag\": \"INNTEKTSKILDE_ANNET\",\n" +
            "                                \"sporsmalstekst\": \"Annet\",\n" +
            "                                \"undertekst\": null,\n" +
            "                                \"svartype\": \"CHECKBOX\",\n" +
            "                                \"min\": null,\n" +
            "                                \"max\": null,\n" +
            "                                \"kriterieForVisningAvUndersporsmal\": null,\n" +
            "                                \"svar\": [],\n" +
            "                                \"undersporsmal\": []\n" +
            "                            }\n" +
            "                        ]\n" +
            "                    }\n" +
            "                ]\n" +
            "            },\n" +
            "            {\n" +
            "                \"id\": \"22978\",\n" +
            "                \"tag\": \"UTLAND\",\n" +
            "                \"sporsmalstekst\": \"Har du oppholdt deg utenfor Norge i perioden 28.09.2018 - 15.10.2018?\",\n" +
            "                \"undertekst\": null,\n" +
            "                \"svartype\": \"JA_NEI\",\n" +
            "                \"min\": null,\n" +
            "                \"max\": null,\n" +
            "                \"kriterieForVisningAvUndersporsmal\": \"JA\",\n" +
            "                \"svar\": [\n" +
            "                    {\n" +
            "                        \"verdi\": \"JA\"\n" +
            "                    }\n" +
            "                ],\n" +
            "                \"undersporsmal\": [\n" +
            "                    {\n" +
            "                        \"id\": \"22979\",\n" +
            "                        \"tag\": \"PERIODER\",\n" +
            "                        \"sporsmalstekst\": \"Når oppholdt du deg utenfor Norge?\",\n" +
            "                        \"undertekst\": null,\n" +
            "                        \"svartype\": \"PERIODER\",\n" +
            "                        \"min\": \"2018-09-28\",\n" +
            "                        \"max\": \"2018-10-15\",\n" +
            "                        \"kriterieForVisningAvUndersporsmal\": null,\n" +
            "                        \"svar\": [\n" +
            "                            {\n" +
            "                                \"verdi\": \"{\\\"fom\\\":\\\"2018-10-11\\\",\\\"tom\\\":\\\"2018-10-14\\\"}\"\n" +
            "                            }\n" +
            "                        ],\n" +
            "                        \"undersporsmal\": []\n" +
            "                    },\n" +
            "                    {\n" +
            "                        \"id\": \"22980\",\n" +
            "                        \"tag\": \"UTLANDSOPPHOLD_SOKT_SYKEPENGER\",\n" +
            "                        \"sporsmalstekst\": \"Har du søkt om å beholde sykepenger under dette oppholdet utenfor Norge?\",\n" +
            "                        \"undertekst\": null,\n" +
            "                        \"svartype\": \"JA_NEI\",\n" +
            "                        \"min\": null,\n" +
            "                        \"max\": null,\n" +
            "                        \"kriterieForVisningAvUndersporsmal\": \"NEI\",\n" +
            "                        \"svar\": [\n" +
            "                            {\n" +
            "                                \"verdi\": \"JA\"\n" +
            "                            }\n" +
            "                        ],\n" +
            "                        \"undersporsmal\": [\n" +
            "                            {\n" +
            "                                \"id\": \"22981\",\n" +
            "                                \"tag\": \"IKKE_SOKT_UTENLANDSOPPHOLD_INFORMASJON\",\n" +
            "                                \"sporsmalstekst\": null,\n" +
            "                                \"undertekst\": \"<p>Som hovedregel kan du bare få sykepenger når du oppholder deg i Norge. Du kan lese mer om <a target=\\\"_blank\\\" href=\\\"https://www.nav.no/no/Person/Arbeid/Sykmeldt%2C+arbeidsavklaringspenger+og+yrkesskade/Sykepenger/sykepenger-ved-utenlandsopphold\\\">sykepenger under utenlandsopphold på denne siden</a>.</p>\",\n" +
            "                                \"svartype\": \"IKKE_RELEVANT\",\n" +
            "                                \"min\": null,\n" +
            "                                \"max\": null,\n" +
            "                                \"kriterieForVisningAvUndersporsmal\": null,\n" +
            "                                \"svar\": [],\n" +
            "                                \"undersporsmal\": []\n" +
            "                            }\n" +
            "                        ]\n" +
            "                    }\n" +
            "                ]\n" +
            "            },\n" +
            "            {\n" +
            "                \"id\": \"22982\",\n" +
            "                \"tag\": \"UTDANNING\",\n" +
            "                \"sporsmalstekst\": \"Har du vært under utdanning i løpet av perioden 28.09.2018 - 15.10.2018?\",\n" +
            "                \"undertekst\": null,\n" +
            "                \"svartype\": \"JA_NEI\",\n" +
            "                \"min\": null,\n" +
            "                \"max\": null,\n" +
            "                \"kriterieForVisningAvUndersporsmal\": \"JA\",\n" +
            "                \"svar\": [\n" +
            "                    {\n" +
            "                        \"verdi\": \"JA\"\n" +
            "                    }\n" +
            "                ],\n" +
            "                \"undersporsmal\": [\n" +
            "                    {\n" +
            "                        \"id\": \"22983\",\n" +
            "                        \"tag\": \"UTDANNING_START\",\n" +
            "                        \"sporsmalstekst\": \"Når startet du på utdanningen?\",\n" +
            "                        \"undertekst\": null,\n" +
            "                        \"svartype\": \"DATO\",\n" +
            "                        \"min\": null,\n" +
            "                        \"max\": \"2018-10-15\",\n" +
            "                        \"kriterieForVisningAvUndersporsmal\": null,\n" +
            "                        \"svar\": [\n" +
            "                            {\n" +
            "                                \"verdi\": \"2018-10-11\"\n" +
            "                            }\n" +
            "                        ],\n" +
            "                        \"undersporsmal\": []\n" +
            "                    },\n" +
            "                    {\n" +
            "                        \"id\": \"22984\",\n" +
            "                        \"tag\": \"FULLTIDSSTUDIUM\",\n" +
            "                        \"sporsmalstekst\": \"Er utdanningen et fulltidsstudium?\",\n" +
            "                        \"undertekst\": null,\n" +
            "                        \"svartype\": \"JA_NEI\",\n" +
            "                        \"min\": null,\n" +
            "                        \"max\": null,\n" +
            "                        \"kriterieForVisningAvUndersporsmal\": null,\n" +
            "                        \"svar\": [\n" +
            "                            {\n" +
            "                                \"verdi\": \"NEI\"\n" +
            "                            }\n" +
            "                        ],\n" +
            "                        \"undersporsmal\": []\n" +
            "                    }\n" +
            "                ]\n" +
            "            },\n" +
            "            {\n" +
            "                \"id\": \"22985\",\n" +
            "                \"tag\": \"BEKREFT_OPPLYSNINGER\",\n" +
            "                \"sporsmalstekst\": \"Jeg har lest all informasjonen jeg har fått i søknaden og bekrefter at opplysningene jeg har gitt er korrekte.\",\n" +
            "                \"undertekst\": null,\n" +
            "                \"svartype\": \"CHECKBOX\",\n" +
            "                \"min\": null,\n" +
            "                \"max\": null,\n" +
            "                \"kriterieForVisningAvUndersporsmal\": null,\n" +
            "                \"svar\": [\n" +
            "                    {\n" +
            "                        \"verdi\": \"CHECKED\"\n" +
            "                    }\n" +
            "                ],\n" +
            "                \"undersporsmal\": []\n" +
            "            },\n" +
            "            {\n" +
            "                \"id\": \"22986\",\n" +
            "                \"tag\": \"VAER_KLAR_OVER_AT\",\n" +
            "                \"sporsmalstekst\": \"Vær klar over at:\",\n" +
            "                \"undertekst\": \"<ul><li>rett til sykepenger forutsetter at du er borte fra arbeid på grunn av egen sykdom. Sosiale eller økonomiske problemer gir ikke rett til sykepenger</li><li>du kan miste retten til sykepenger hvis du uten rimelig grunn nekter å opplyse om egen funksjonsevne eller nekter å ta imot tilbud om behandling og/eller tilrettelegging</li><li>sykepenger utbetales i maksimum 52 uker, også for gradert (delvis) sykmelding</li><li>fristen for å søke sykepenger er som hovedregel 3 måneder</li></ul>\",\n" +
            "                \"svartype\": \"IKKE_RELEVANT\",\n" +
            "                \"min\": null,\n" +
            "                \"max\": null,\n" +
            "                \"kriterieForVisningAvUndersporsmal\": null,\n" +
            "                \"svar\": [],\n" +
            "                \"undersporsmal\": []\n" +
            "            }\n" +
            "        ]\n" +
            "    }";

    public static final String beskrivelseSelvstendigMangeSvar = "Søknad om sykepenger for frilanser for perioden 28.09.2018 - 15.10.2018\n" +
            "\n" +
            "Var du tilbake i fullt arbeid som frilanser før sykmeldingsperioden utløp 15.10.2018?\n" +
            "Ja\n" +
            "    Når var du tilbake i arbeid?\n" +
            "    11.10.2018\n" +
            "\n" +
            "I perioden 28.09.2018 - 06.10.2018 skulle du ifølge sykmeldingen jobbe 60% som frilanser. Jobbet du mer enn dette?\n" +
            "Nei\n" +
            "\n" +
            "I perioden 07.10.2018 - 15.10.2018 var du 100% sykmeldt som frilanser. Jobbet du noe i denne perioden?\n" +
            "Ja\n" +
            "    Hvor mange timer jobber du normalt per uke som frilanser?\n" +
            "    40 timer\n" +
            "\n" +
            "    Hvor mye jobbet du totalt i denne perioden som frilanser?\n" +
            "    54 prosent\n" +
            "\n" +
            "Har du andre inntektskilder eller arbeidsforhold?\n" +
            "Ja\n" +
            "    Hvilke andre inntektskilder har du?\n" +
            "        Jordbruker / Fisker / Reindriftsutøver\n" +
            "            Er du sykmeldt fra dette?\n" +
            "            Ja\n" +
            "\n" +
            "        Selvstendig næringsdrivende\n" +
            "            Er du sykmeldt fra dette?\n" +
            "            Nei\n" +
            "\n" +
            "Har du oppholdt deg utenfor Norge i perioden 28.09.2018 - 15.10.2018?\n" +
            "Ja\n" +
            "    Når oppholdt du deg utenfor Norge?\n" +
            "    11.10.2018 - 14.10.2018\n" +
            "\n" +
            "    Har du søkt om å beholde sykepenger under dette oppholdet utenfor Norge?\n" +
            "    Ja\n" +
            "\n" +
            "Har du vært under utdanning i løpet av perioden 28.09.2018 - 15.10.2018?\n" +
            "Ja\n" +
            "    Når startet du på utdanningen?\n" +
            "    11.10.2018\n" +
            "\n" +
            "    Er utdanningen et fulltidsstudium?\n" +
            "    Nei\n";

    public static final String soknadSelvstendigMedNeisvar = "{\n" +
            "        \"id\": \"8631c48c-460c-4386-ba52-6854e1fcd354\",\n" +
            "        \"aktorId\": \"aktorId\",\n" +
            "        \"sykmeldingId\": \"8a30a0ec-4832-4c46-b934-b685b78e3fee\",\n" +
            "        \"soknadstype\": \"SELVSTENDIGE_OG_FRILANSERE\",\n" +
            "        \"status\": \"SENDT\",\n" +
            "        \"fom\": \"2018-09-29\",\n" +
            "        \"tom\": \"2018-10-16\",\n" +
            "        \"opprettetDato\": \"2018-10-17\",\n" +
            "        \"innsendtDato\": \"2018-10-17\",\n" +
            "        \"korrigerer\": null,\n" +
            "        \"korrigertAv\": null,\n" +
            "        \"sporsmal\": [\n" +
            "            {\n" +
            "                \"id\": \"23059\",\n" +
            "                \"tag\": \"ANSVARSERKLARING\",\n" +
            "                \"sporsmalstekst\": \"Jeg vet at dersom jeg gir uriktige opplysninger, eller holder tilbake opplysninger som har betydning for min rett til sykepenger, kan pengene holdes tilbake eller kreves tilbake, og/eller det kan medføre straffeansvar. Jeg er også klar over at jeg må melde fra til NAV dersom jeg i sykmeldingsperioden satt i varetekt, sonet straff eller var under forvaring.\",\n" +
            "                \"undertekst\": null,\n" +
            "                \"svartype\": \"CHECKBOX\",\n" +
            "                \"min\": null,\n" +
            "                \"max\": null,\n" +
            "                \"kriterieForVisningAvUndersporsmal\": null,\n" +
            "                \"svar\": [\n" +
            "                    {\n" +
            "                        \"verdi\": \"CHECKED\"\n" +
            "                    }\n" +
            "                ],\n" +
            "                \"undersporsmal\": []\n" +
            "            },\n" +
            "            {\n" +
            "                \"id\": \"23060\",\n" +
            "                \"tag\": \"TILBAKE_I_ARBEID\",\n" +
            "                \"sporsmalstekst\": \"Var du tilbake i fullt arbeid som frilanser før sykmeldingsperioden utløp 16.10.2018?\",\n" +
            "                \"undertekst\": null,\n" +
            "                \"svartype\": \"JA_NEI\",\n" +
            "                \"min\": null,\n" +
            "                \"max\": null,\n" +
            "                \"kriterieForVisningAvUndersporsmal\": \"JA\",\n" +
            "                \"svar\": [\n" +
            "                    {\n" +
            "                        \"verdi\": \"NEI\"\n" +
            "                    }\n" +
            "                ],\n" +
            "                \"undersporsmal\": [\n" +
            "                    {\n" +
            "                        \"id\": \"23061\",\n" +
            "                        \"tag\": \"TILBAKE_NAR\",\n" +
            "                        \"sporsmalstekst\": \"Når var du tilbake i arbeid?\",\n" +
            "                        \"undertekst\": null,\n" +
            "                        \"svartype\": \"DATO\",\n" +
            "                        \"min\": \"2018-09-29\",\n" +
            "                        \"max\": \"2018-10-17\",\n" +
            "                        \"kriterieForVisningAvUndersporsmal\": null,\n" +
            "                        \"svar\": [],\n" +
            "                        \"undersporsmal\": []\n" +
            "                    }\n" +
            "                ]\n" +
            "            },\n" +
            "            {\n" +
            "                \"id\": \"23062\",\n" +
            "                \"tag\": \"JOBBET_DU_GRADERT_0\",\n" +
            "                \"sporsmalstekst\": \"I perioden 29.09.2018 - 07.10.2018 skulle du ifølge sykmeldingen jobbe 60% som frilanser. Jobbet du mer enn dette?\",\n" +
            "                \"undertekst\": null,\n" +
            "                \"svartype\": \"JA_NEI\",\n" +
            "                \"min\": null,\n" +
            "                \"max\": null,\n" +
            "                \"kriterieForVisningAvUndersporsmal\": \"JA\",\n" +
            "                \"svar\": [\n" +
            "                    {\n" +
            "                        \"verdi\": \"NEI\"\n" +
            "                    }\n" +
            "                ],\n" +
            "                \"undersporsmal\": [\n" +
            "                    {\n" +
            "                        \"id\": \"23063\",\n" +
            "                        \"tag\": \"HVOR_MANGE_TIMER_0\",\n" +
            "                        \"sporsmalstekst\": \"Hvor mange timer jobber du normalt per uke som frilanser?\",\n" +
            "                        \"undertekst\": null,\n" +
            "                        \"svartype\": \"TIMER\",\n" +
            "                        \"min\": \"1\",\n" +
            "                        \"max\": \"150\",\n" +
            "                        \"kriterieForVisningAvUndersporsmal\": null,\n" +
            "                        \"svar\": [],\n" +
            "                        \"undersporsmal\": []\n" +
            "                    },\n" +
            "                    {\n" +
            "                        \"id\": \"23064\",\n" +
            "                        \"tag\": \"HVOR_MYE_HAR_DU_JOBBET_0\",\n" +
            "                        \"sporsmalstekst\": \"Hvor mye jobbet du totalt i denne perioden som frilanser?\",\n" +
            "                        \"undertekst\": null,\n" +
            "                        \"svartype\": \"PROSENT\",\n" +
            "                        \"min\": \"61\",\n" +
            "                        \"max\": \"99\",\n" +
            "                        \"kriterieForVisningAvUndersporsmal\": null,\n" +
            "                        \"svar\": [],\n" +
            "                        \"undersporsmal\": []\n" +
            "                    }\n" +
            "                ]\n" +
            "            },\n" +
            "            {\n" +
            "                \"id\": \"23065\",\n" +
            "                \"tag\": \"JOBBET_DU_100_PROSENT_1\",\n" +
            "                \"sporsmalstekst\": \"I perioden 08.10.2018 - 16.10.2018 var du 100% sykmeldt som frilanser. Jobbet du noe i denne perioden?\",\n" +
            "                \"undertekst\": null,\n" +
            "                \"svartype\": \"JA_NEI\",\n" +
            "                \"min\": null,\n" +
            "                \"max\": null,\n" +
            "                \"kriterieForVisningAvUndersporsmal\": \"JA\",\n" +
            "                \"svar\": [\n" +
            "                    {\n" +
            "                        \"verdi\": \"NEI\"\n" +
            "                    }\n" +
            "                ],\n" +
            "                \"undersporsmal\": [\n" +
            "                    {\n" +
            "                        \"id\": \"23066\",\n" +
            "                        \"tag\": \"HVOR_MANGE_TIMER_1\",\n" +
            "                        \"sporsmalstekst\": \"Hvor mange timer jobber du normalt per uke som frilanser?\",\n" +
            "                        \"undertekst\": null,\n" +
            "                        \"svartype\": \"TIMER\",\n" +
            "                        \"min\": \"1\",\n" +
            "                        \"max\": \"150\",\n" +
            "                        \"kriterieForVisningAvUndersporsmal\": null,\n" +
            "                        \"svar\": [],\n" +
            "                        \"undersporsmal\": []\n" +
            "                    },\n" +
            "                    {\n" +
            "                        \"id\": \"23067\",\n" +
            "                        \"tag\": \"HVOR_MYE_HAR_DU_JOBBET_1\",\n" +
            "                        \"sporsmalstekst\": \"Hvor mye jobbet du totalt i denne perioden som frilanser?\",\n" +
            "                        \"undertekst\": null,\n" +
            "                        \"svartype\": \"PROSENT\",\n" +
            "                        \"min\": \"1\",\n" +
            "                        \"max\": \"99\",\n" +
            "                        \"kriterieForVisningAvUndersporsmal\": null,\n" +
            "                        \"svar\": [],\n" +
            "                        \"undersporsmal\": []\n" +
            "                    }\n" +
            "                ]\n" +
            "            },\n" +
            "            {\n" +
            "                \"id\": \"23068\",\n" +
            "                \"tag\": \"ANDRE_INNTEKTSKILDER\",\n" +
            "                \"sporsmalstekst\": \"Har du andre inntektskilder eller arbeidsforhold?\",\n" +
            "                \"undertekst\": null,\n" +
            "                \"svartype\": \"JA_NEI\",\n" +
            "                \"min\": null,\n" +
            "                \"max\": null,\n" +
            "                \"kriterieForVisningAvUndersporsmal\": \"JA\",\n" +
            "                \"svar\": [\n" +
            "                    {\n" +
            "                        \"verdi\": \"NEI\"\n" +
            "                    }\n" +
            "                ],\n" +
            "                \"undersporsmal\": [\n" +
            "                    {\n" +
            "                        \"id\": \"23069\",\n" +
            "                        \"tag\": \"HVILKE_ANDRE_INNTEKTSKILDER\",\n" +
            "                        \"sporsmalstekst\": \"Hvilke andre inntektskilder har du?\",\n" +
            "                        \"undertekst\": \"Du trenger ikke oppgi andre ytelser fra NAV\",\n" +
            "                        \"svartype\": \"CHECKBOX_GRUPPE\",\n" +
            "                        \"min\": null,\n" +
            "                        \"max\": null,\n" +
            "                        \"kriterieForVisningAvUndersporsmal\": null,\n" +
            "                        \"svar\": [],\n" +
            "                        \"undersporsmal\": [\n" +
            "                            {\n" +
            "                                \"id\": \"23070\",\n" +
            "                                \"tag\": \"INNTEKTSKILDE_ARBEIDSFORHOLD\",\n" +
            "                                \"sporsmalstekst\": \"Arbeidsforhold\",\n" +
            "                                \"undertekst\": null,\n" +
            "                                \"svartype\": \"CHECKBOX\",\n" +
            "                                \"min\": null,\n" +
            "                                \"max\": null,\n" +
            "                                \"kriterieForVisningAvUndersporsmal\": \"CHECKED\",\n" +
            "                                \"svar\": [],\n" +
            "                                \"undersporsmal\": [\n" +
            "                                    {\n" +
            "                                        \"id\": \"23071\",\n" +
            "                                        \"tag\": \"INNTEKTSKILDE_ARBEIDSFORHOLD_ER_DU_SYKMELDT\",\n" +
            "                                        \"sporsmalstekst\": \"Er du sykmeldt fra dette?\",\n" +
            "                                        \"undertekst\": null,\n" +
            "                                        \"svartype\": \"JA_NEI\",\n" +
            "                                        \"min\": null,\n" +
            "                                        \"max\": null,\n" +
            "                                        \"kriterieForVisningAvUndersporsmal\": null,\n" +
            "                                        \"svar\": [],\n" +
            "                                        \"undersporsmal\": []\n" +
            "                                    }\n" +
            "                                ]\n" +
            "                            },\n" +
            "                            {\n" +
            "                                \"id\": \"23072\",\n" +
            "                                \"tag\": \"INNTEKTSKILDE_JORDBRUKER\",\n" +
            "                                \"sporsmalstekst\": \"Jordbruker / Fisker / Reindriftsutøver\",\n" +
            "                                \"undertekst\": null,\n" +
            "                                \"svartype\": \"CHECKBOX\",\n" +
            "                                \"min\": null,\n" +
            "                                \"max\": null,\n" +
            "                                \"kriterieForVisningAvUndersporsmal\": \"CHECKED\",\n" +
            "                                \"svar\": [],\n" +
            "                                \"undersporsmal\": [\n" +
            "                                    {\n" +
            "                                        \"id\": \"23073\",\n" +
            "                                        \"tag\": \"INNTEKTSKILDE_JORDBRUKER_ER_DU_SYKMELDT\",\n" +
            "                                        \"sporsmalstekst\": \"Er du sykmeldt fra dette?\",\n" +
            "                                        \"undertekst\": null,\n" +
            "                                        \"svartype\": \"JA_NEI\",\n" +
            "                                        \"min\": null,\n" +
            "                                        \"max\": null,\n" +
            "                                        \"kriterieForVisningAvUndersporsmal\": null,\n" +
            "                                        \"svar\": [],\n" +
            "                                        \"undersporsmal\": []\n" +
            "                                    }\n" +
            "                                ]\n" +
            "                            },\n" +
            "                            {\n" +
            "                                \"id\": \"23074\",\n" +
            "                                \"tag\": \"INNTEKTSKILDE_FRILANSER_SELVSTENDIG\",\n" +
            "                                \"sporsmalstekst\": \"Selvstendig næringsdrivende\",\n" +
            "                                \"undertekst\": null,\n" +
            "                                \"svartype\": \"CHECKBOX\",\n" +
            "                                \"min\": null,\n" +
            "                                \"max\": null,\n" +
            "                                \"kriterieForVisningAvUndersporsmal\": \"CHECKED\",\n" +
            "                                \"svar\": [],\n" +
            "                                \"undersporsmal\": [\n" +
            "                                    {\n" +
            "                                        \"id\": \"23075\",\n" +
            "                                        \"tag\": \"INNTEKTSKILDE_FRILANSER_SELVSTENDIG_ER_DU_SYKMELDT\",\n" +
            "                                        \"sporsmalstekst\": \"Er du sykmeldt fra dette?\",\n" +
            "                                        \"undertekst\": null,\n" +
            "                                        \"svartype\": \"JA_NEI\",\n" +
            "                                        \"min\": null,\n" +
            "                                        \"max\": null,\n" +
            "                                        \"kriterieForVisningAvUndersporsmal\": null,\n" +
            "                                        \"svar\": [],\n" +
            "                                        \"undersporsmal\": []\n" +
            "                                    }\n" +
            "                                ]\n" +
            "                            },\n" +
            "                            {\n" +
            "                                \"id\": \"23076\",\n" +
            "                                \"tag\": \"INNTEKTSKILDE_ANNET\",\n" +
            "                                \"sporsmalstekst\": \"Annet\",\n" +
            "                                \"undertekst\": null,\n" +
            "                                \"svartype\": \"CHECKBOX\",\n" +
            "                                \"min\": null,\n" +
            "                                \"max\": null,\n" +
            "                                \"kriterieForVisningAvUndersporsmal\": null,\n" +
            "                                \"svar\": [],\n" +
            "                                \"undersporsmal\": []\n" +
            "                            }\n" +
            "                        ]\n" +
            "                    }\n" +
            "                ]\n" +
            "            },\n" +
            "            {\n" +
            "                \"id\": \"23077\",\n" +
            "                \"tag\": \"UTLAND\",\n" +
            "                \"sporsmalstekst\": \"Har du oppholdt deg utenfor Norge i perioden 29.09.2018 - 16.10.2018?\",\n" +
            "                \"undertekst\": null,\n" +
            "                \"svartype\": \"JA_NEI\",\n" +
            "                \"min\": null,\n" +
            "                \"max\": null,\n" +
            "                \"kriterieForVisningAvUndersporsmal\": \"JA\",\n" +
            "                \"svar\": [\n" +
            "                    {\n" +
            "                        \"verdi\": \"NEI\"\n" +
            "                    }\n" +
            "                ],\n" +
            "                \"undersporsmal\": [\n" +
            "                    {\n" +
            "                        \"id\": \"23078\",\n" +
            "                        \"tag\": \"PERIODER\",\n" +
            "                        \"sporsmalstekst\": \"Når oppholdt du deg utenfor Norge?\",\n" +
            "                        \"undertekst\": null,\n" +
            "                        \"svartype\": \"PERIODER\",\n" +
            "                        \"min\": \"2018-09-29\",\n" +
            "                        \"max\": \"2018-10-16\",\n" +
            "                        \"kriterieForVisningAvUndersporsmal\": null,\n" +
            "                        \"svar\": [],\n" +
            "                        \"undersporsmal\": []\n" +
            "                    },\n" +
            "                    {\n" +
            "                        \"id\": \"23079\",\n" +
            "                        \"tag\": \"UTLANDSOPPHOLD_SOKT_SYKEPENGER\",\n" +
            "                        \"sporsmalstekst\": \"Har du søkt om å beholde sykepenger under dette oppholdet utenfor Norge?\",\n" +
            "                        \"undertekst\": null,\n" +
            "                        \"svartype\": \"JA_NEI\",\n" +
            "                        \"min\": null,\n" +
            "                        \"max\": null,\n" +
            "                        \"kriterieForVisningAvUndersporsmal\": \"NEI\",\n" +
            "                        \"svar\": [],\n" +
            "                        \"undersporsmal\": [\n" +
            "                            {\n" +
            "                                \"id\": \"23080\",\n" +
            "                                \"tag\": \"IKKE_SOKT_UTENLANDSOPPHOLD_INFORMASJON\",\n" +
            "                                \"sporsmalstekst\": null,\n" +
            "                                \"undertekst\": \"<p>Som hovedregel kan du bare få sykepenger når du oppholder deg i Norge. Du kan lese mer om <a target=\\\"_blank\\\" href=\\\"https://www.nav.no/no/Person/Arbeid/Sykmeldt%2C+arbeidsavklaringspenger+og+yrkesskade/Sykepenger/sykepenger-ved-utenlandsopphold\\\">sykepenger under utenlandsopphold på denne siden</a>.</p>\",\n" +
            "                                \"svartype\": \"IKKE_RELEVANT\",\n" +
            "                                \"min\": null,\n" +
            "                                \"max\": null,\n" +
            "                                \"kriterieForVisningAvUndersporsmal\": null,\n" +
            "                                \"svar\": [],\n" +
            "                                \"undersporsmal\": []\n" +
            "                            }\n" +
            "                        ]\n" +
            "                    }\n" +
            "                ]\n" +
            "            },\n" +
            "            {\n" +
            "                \"id\": \"23081\",\n" +
            "                \"tag\": \"UTDANNING\",\n" +
            "                \"sporsmalstekst\": \"Har du vært under utdanning i løpet av perioden 29.09.2018 - 16.10.2018?\",\n" +
            "                \"undertekst\": null,\n" +
            "                \"svartype\": \"JA_NEI\",\n" +
            "                \"min\": null,\n" +
            "                \"max\": null,\n" +
            "                \"kriterieForVisningAvUndersporsmal\": \"JA\",\n" +
            "                \"svar\": [\n" +
            "                    {\n" +
            "                        \"verdi\": \"NEI\"\n" +
            "                    }\n" +
            "                ],\n" +
            "                \"undersporsmal\": [\n" +
            "                    {\n" +
            "                        \"id\": \"23082\",\n" +
            "                        \"tag\": \"UTDANNING_START\",\n" +
            "                        \"sporsmalstekst\": \"Når startet du på utdanningen?\",\n" +
            "                        \"undertekst\": null,\n" +
            "                        \"svartype\": \"DATO\",\n" +
            "                        \"min\": null,\n" +
            "                        \"max\": \"2018-10-16\",\n" +
            "                        \"kriterieForVisningAvUndersporsmal\": null,\n" +
            "                        \"svar\": [],\n" +
            "                        \"undersporsmal\": []\n" +
            "                    },\n" +
            "                    {\n" +
            "                        \"id\": \"23083\",\n" +
            "                        \"tag\": \"FULLTIDSSTUDIUM\",\n" +
            "                        \"sporsmalstekst\": \"Er utdanningen et fulltidsstudium?\",\n" +
            "                        \"undertekst\": null,\n" +
            "                        \"svartype\": \"JA_NEI\",\n" +
            "                        \"min\": null,\n" +
            "                        \"max\": null,\n" +
            "                        \"kriterieForVisningAvUndersporsmal\": null,\n" +
            "                        \"svar\": [],\n" +
            "                        \"undersporsmal\": []\n" +
            "                    }\n" +
            "                ]\n" +
            "            },\n" +
            "            {\n" +
            "                \"id\": \"23084\",\n" +
            "                \"tag\": \"BEKREFT_OPPLYSNINGER\",\n" +
            "                \"sporsmalstekst\": \"Jeg har lest all informasjonen jeg har fått i søknaden og bekrefter at opplysningene jeg har gitt er korrekte.\",\n" +
            "                \"undertekst\": null,\n" +
            "                \"svartype\": \"CHECKBOX\",\n" +
            "                \"min\": null,\n" +
            "                \"max\": null,\n" +
            "                \"kriterieForVisningAvUndersporsmal\": null,\n" +
            "                \"svar\": [\n" +
            "                    {\n" +
            "                        \"verdi\": \"CHECKED\"\n" +
            "                    }\n" +
            "                ],\n" +
            "                \"undersporsmal\": []\n" +
            "            },\n" +
            "            {\n" +
            "                \"id\": \"23085\",\n" +
            "                \"tag\": \"VAER_KLAR_OVER_AT\",\n" +
            "                \"sporsmalstekst\": \"Vær klar over at:\",\n" +
            "                \"undertekst\": \"<ul><li>rett til sykepenger forutsetter at du er borte fra arbeid på grunn av egen sykdom. Sosiale eller økonomiske problemer gir ikke rett til sykepenger</li><li>du kan miste retten til sykepenger hvis du uten rimelig grunn nekter å opplyse om egen funksjonsevne eller nekter å ta imot tilbud om behandling og/eller tilrettelegging</li><li>sykepenger utbetales i maksimum 52 uker, også for gradert (delvis) sykmelding</li><li>fristen for å søke sykepenger er som hovedregel 3 måneder</li></ul>\",\n" +
            "                \"svartype\": \"IKKE_RELEVANT\",\n" +
            "                \"min\": null,\n" +
            "                \"max\": null,\n" +
            "                \"kriterieForVisningAvUndersporsmal\": null,\n" +
            "                \"svar\": [],\n" +
            "                \"undersporsmal\": []\n" +
            "            }\n" +
            "        ]\n" +
            "    }";

    public static final String beskrivelseSoknadSelvstendigMedNeisvar = "Søknad om sykepenger for frilanser for perioden 29.09.2018 - 16.10.2018\n" +
            "\n" +
            "I perioden 29.09.2018 - 07.10.2018 skulle du ifølge sykmeldingen jobbe 60% som frilanser. Jobbet du mer enn dette?\n" +
            "Nei\n" +
            "\n" +
            "I perioden 08.10.2018 - 16.10.2018 var du 100% sykmeldt som frilanser. Jobbet du noe i denne perioden?\n" +
            "Nei\n";

    public static final String soknadUtland = "{\n" +
            "        \"id\": \"6311fa0e-d4d5-49a1-a6d5-c253d569f715\",\n" +
            "        \"aktorId\": \"aktorId\",\n" +
            "        \"sykmeldingId\": null,\n" +
            "        \"soknadstype\": \"OPPHOLD_UTLAND\",\n" +
            "        \"status\": \"SENDT\",\n" +
            "        \"fom\": null,\n" +
            "        \"tom\": null,\n" +
            "        \"opprettetDato\": \"2018-10-17\",\n" +
            "        \"innsendtDato\": \"2018-10-17\",\n" +
            "        \"korrigerer\": null,\n" +
            "        \"korrigertAv\": null,\n" +
            "        \"sporsmal\": [\n" +
            "            {\n" +
            "                \"id\": \"23117\",\n" +
            "                \"tag\": \"PERIODEUTLAND\",\n" +
            "                \"sporsmalstekst\": \"Når skal du være utenfor Norge?\",\n" +
            "                \"undertekst\": null,\n" +
            "                \"svartype\": \"PERIODER\",\n" +
            "                \"min\": \"2018-07-17\",\n" +
            "                \"max\": \"2019-04-17\",\n" +
            "                \"kriterieForVisningAvUndersporsmal\": null,\n" +
            "                \"svar\": [\n" +
            "                    {\n" +
            "                        \"verdi\": \"{\\\"fom\\\":\\\"2018-10-10\\\",\\\"tom\\\":\\\"2018-10-18\\\"}\"\n" +
            "                    }\n" +
            "                ],\n" +
            "                \"undersporsmal\": []\n" +
            "            },\n" +
            "            {\n" +
            "                \"id\": \"23118\",\n" +
            "                \"tag\": \"LAND\",\n" +
            "                \"sporsmalstekst\": \"Hvor skal du reise?\",\n" +
            "                \"undertekst\": null,\n" +
            "                \"svartype\": \"FRITEKST\",\n" +
            "                \"min\": null,\n" +
            "                \"max\": \"50\",\n" +
            "                \"kriterieForVisningAvUndersporsmal\": null,\n" +
            "                \"svar\": [\n" +
            "                    {\n" +
            "                        \"verdi\": \"Laksefiske\"\n" +
            "                    }\n" +
            "                ],\n" +
            "                \"undersporsmal\": []\n" +
            "            },\n" +
            "            {\n" +
            "                \"id\": \"23119\",\n" +
            "                \"tag\": \"ARBEIDSGIVER\",\n" +
            "                \"sporsmalstekst\": \"Har du arbeidsgiver?\",\n" +
            "                \"undertekst\": null,\n" +
            "                \"svartype\": \"JA_NEI\",\n" +
            "                \"min\": null,\n" +
            "                \"max\": null,\n" +
            "                \"kriterieForVisningAvUndersporsmal\": \"JA\",\n" +
            "                \"svar\": [\n" +
            "                    {\n" +
            "                        \"verdi\": \"JA\"\n" +
            "                    }\n" +
            "                ],\n" +
            "                \"undersporsmal\": [\n" +
            "                    {\n" +
            "                        \"id\": \"23120\",\n" +
            "                        \"tag\": \"SYKMELDINGSGRAD\",\n" +
            "                        \"sporsmalstekst\": \"Er du 100 % sykmeldt?\",\n" +
            "                        \"undertekst\": null,\n" +
            "                        \"svartype\": \"JA_NEI\",\n" +
            "                        \"min\": null,\n" +
            "                        \"max\": null,\n" +
            "                        \"kriterieForVisningAvUndersporsmal\": null,\n" +
            "                        \"svar\": [\n" +
            "                            {\n" +
            "                                \"verdi\": \"JA\"\n" +
            "                            }\n" +
            "                        ],\n" +
            "                        \"undersporsmal\": []\n" +
            "                    },\n" +
            "                    {\n" +
            "                        \"id\": \"23121\",\n" +
            "                        \"tag\": \"FERIE\",\n" +
            "                        \"sporsmalstekst\": \"Har du avtalt med arbeidsgiveren din at du skal ha ferie i hele perioden?\",\n" +
            "                        \"undertekst\": null,\n" +
            "                        \"svartype\": \"JA_NEI\",\n" +
            "                        \"min\": null,\n" +
            "                        \"max\": null,\n" +
            "                        \"kriterieForVisningAvUndersporsmal\": null,\n" +
            "                        \"svar\": [\n" +
            "                            {\n" +
            "                                \"verdi\": \"NEI\"\n" +
            "                            }\n" +
            "                        ],\n" +
            "                        \"undersporsmal\": []\n" +
            "                    }\n" +
            "                ]\n" +
            "            },\n" +
            "            {\n" +
            "                \"id\": \"23122\",\n" +
            "                \"tag\": \"BEKREFT_OPPLYSNINGER_UTLAND_INFO\",\n" +
            "                \"sporsmalstekst\": \"Før du reiser ber vi deg bekrefte:\",\n" +
            "                \"undertekst\": \"<ul>\\n    <li>Reisen vil ikke gjøre at jeg blir dårligere </li>\\n    <li>Reisen vil ikke gjøre at sykefraværet blir lengre</li>\\n    <li>Reisen vil ikke hindre planlagt behandling eller oppfølging fra NAV eller arbeidsgiveren min</li>\\n</ul>\",\n" +
            "                \"svartype\": \"IKKE_RELEVANT\",\n" +
            "                \"min\": null,\n" +
            "                \"max\": null,\n" +
            "                \"kriterieForVisningAvUndersporsmal\": null,\n" +
            "                \"svar\": [],\n" +
            "                \"undersporsmal\": [\n" +
            "                    {\n" +
            "                        \"id\": \"23123\",\n" +
            "                        \"tag\": \"BEKREFT_OPPLYSNINGER_UTLAND\",\n" +
            "                        \"sporsmalstekst\": \"Jeg bekrefter de tre punktene ovenfor. Jeg har avklart reisen med legen og arbeidsgiveren min.\",\n" +
            "                        \"undertekst\": null,\n" +
            "                        \"svartype\": \"CHECKBOX_PANEL\",\n" +
            "                        \"min\": null,\n" +
            "                        \"max\": null,\n" +
            "                        \"kriterieForVisningAvUndersporsmal\": null,\n" +
            "                        \"svar\": [\n" +
            "                            {\n" +
            "                                \"verdi\": \"CHECKED\"\n" +
            "                            }\n" +
            "                        ],\n" +
            "                        \"undersporsmal\": []\n" +
            "                    }\n" +
            "                ]\n" +
            "            }\n" +
            "        ]\n" +
            "    }";

    public static final String beskrivelseUtland = "Søknad om å beholde sykepenger i utlandet \n" +
            "\n" +
            "Når skal du være utenfor Norge?\n" +
            "10.10.2018 - 18.10.2018\n" +
            "\n" +
            "Hvor skal du reise?\n" +
            "Laksefiske\n" +
            "\n" +
            "Har du arbeidsgiver?\n" +
            "Ja\n" +
            "    Er du 100 % sykmeldt?\n" +
            "    Ja\n" +
            "\n" +
            "    Har du avtalt med arbeidsgiveren din at du skal ha ferie i hele perioden?\n" +
            "    Nei\n";
}
