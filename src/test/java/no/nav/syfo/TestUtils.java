package no.nav.syfo;

public class TestUtils {

    public static String soknadSelvstendigMangeSvar = "{\n" +
            "        \"id\": \"daa8b4b5-ece8-4e6d-ab7e-c7354958201a\",\n" +
            "        \"aktorId\": \"aktorId-745463060\",\n" +
            "        \"sykmeldingId\": \"14e78e84-50a5-45bb-9919-191c54f99691\",\n" +
            "        \"soknadstype\": \"SELVSTENDIGE_OG_FRILANSERE\",\n" +
            "        \"status\": \"SENDT\",\n" +
            "        \"fom\": \"2018-05-20\",\n" +
            "        \"tom\": \"2018-05-28\",\n" +
            "        \"opprettet\": \"2018-11-16T00:00:00\",\n" +
            "        \"sendtNav\": \"2018-11-16T00:00:00\",\n" +
            "        \"startSykeforlop\": null,\n" +
            "        \"sykmeldingSkrevet\": null,\n" +
            "        \"arbeidsgiver\": null,\n" +
            "        \"korrigerer\": null,\n" +
            "        \"korrigertAv\": null,\n" +
            "        \"arbeidssituasjon\": \"FRILANSER\",\n" +
            "        \"soknadPerioder\": [\n" +
            "            {\n" +
            "                \"fom\": \"2018-05-20\",\n" +
            "                \"tom\": \"2018-05-24\",\n" +
            "                \"grad\": 100\n" +
            "            },\n" +
            "            {\n" +
            "                \"fom\": \"2018-05-25\",\n" +
            "                \"tom\": \"2018-05-28\",\n" +
            "                \"grad\": 40\n" +
            "            }\n" +
            "        ],\n" +
            "        \"sporsmal\": [\n" +
            "            {\n" +
            "                \"id\": \"1\",\n" +
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
            "                \"id\": \"2\",\n" +
            "                \"tag\": \"TILBAKE_I_ARBEID\",\n" +
            "                \"sporsmalstekst\": \"Var du tilbake i fullt arbeid som frilanser før sykmeldingsperioden utløp 28.05.2018?\",\n" +
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
            "                        \"id\": \"3\",\n" +
            "                        \"tag\": \"TILBAKE_NAR\",\n" +
            "                        \"sporsmalstekst\": \"Når var du tilbake i arbeid?\",\n" +
            "                        \"undertekst\": null,\n" +
            "                        \"svartype\": \"DATO\",\n" +
            "                        \"min\": \"2018-05-20\",\n" +
            "                        \"max\": \"2018-05-29\",\n" +
            "                        \"kriterieForVisningAvUndersporsmal\": null,\n" +
            "                        \"svar\": [\n" +
            "                            {\n" +
            "                                \"verdi\": \"2018-05-27\"\n" +
            "                            }\n" +
            "                        ],\n" +
            "                        \"undersporsmal\": []\n" +
            "                    }\n" +
            "                ]\n" +
            "            },\n" +
            "            {\n" +
            "                \"id\": \"4\",\n" +
            "                \"tag\": \"JOBBET_DU_100_PROSENT_0\",\n" +
            "                \"sporsmalstekst\": \"I perioden 20.05.2018 - 24.05.2018 var du 100% sykmeldt som frilanser. Jobbet du noe i denne perioden?\",\n" +
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
            "                        \"id\": \"5\",\n" +
            "                        \"tag\": \"HVOR_MANGE_TIMER_0\",\n" +
            "                        \"sporsmalstekst\": \"Hvor mange timer jobber du normalt per uke som frilanser?\",\n" +
            "                        \"undertekst\": null,\n" +
            "                        \"svartype\": \"TIMER\",\n" +
            "                        \"min\": \"1\",\n" +
            "                        \"max\": \"150\",\n" +
            "                        \"kriterieForVisningAvUndersporsmal\": null,\n" +
            "                        \"svar\": [\n" +
            "                            {\n" +
            "                                \"verdi\": \"37.5\"\n" +
            "                            }\n" +
            "                        ],\n" +
            "                        \"undersporsmal\": []\n" +
            "                    },\n" +
            "                    {\n" +
            "                        \"id\": \"6\",\n" +
            "                        \"tag\": \"HVOR_MYE_HAR_DU_JOBBET_0\",\n" +
            "                        \"sporsmalstekst\": \"Hvor mye jobbet du totalt i denne perioden som frilanser?\",\n" +
            "                        \"undertekst\": null,\n" +
            "                        \"svartype\": \"PROSENT\",\n" +
            "                        \"min\": \"1\",\n" +
            "                        \"max\": \"99\",\n" +
            "                        \"kriterieForVisningAvUndersporsmal\": null,\n" +
            "                        \"svar\": [\n" +
            "                            {\n" +
            "                                \"verdi\": \"40\"\n" +
            "                            }\n" +
            "                        ],\n" +
            "                        \"undersporsmal\": []\n" +
            "                    }\n" +
            "                ]\n" +
            "            },\n" +
            "            {\n" +
            "                \"id\": \"7\",\n" +
            "                \"tag\": \"JOBBET_DU_GRADERT_1\",\n" +
            "                \"sporsmalstekst\": \"I perioden 25.05.2018 - 28.05.2018 skulle du ifølge sykmeldingen jobbe 60% som frilanser. Jobbet du mer enn dette?\",\n" +
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
            "                        \"id\": \"8\",\n" +
            "                        \"tag\": \"HVOR_MANGE_TIMER_1\",\n" +
            "                        \"sporsmalstekst\": \"Hvor mange timer jobber du normalt per uke som frilanser?\",\n" +
            "                        \"undertekst\": null,\n" +
            "                        \"svartype\": \"TIMER\",\n" +
            "                        \"min\": \"1\",\n" +
            "                        \"max\": \"150\",\n" +
            "                        \"kriterieForVisningAvUndersporsmal\": null,\n" +
            "                        \"svar\": [\n" +
            "                            {\n" +
            "                                \"verdi\": \"37.5\"\n" +
            "                            }\n" +
            "                        ],\n" +
            "                        \"undersporsmal\": []\n" +
            "                    },\n" +
            "                    {\n" +
            "                        \"id\": \"9\",\n" +
            "                        \"tag\": \"HVOR_MYE_HAR_DU_JOBBET_1\",\n" +
            "                        \"sporsmalstekst\": \"Hvor mye jobbet du totalt i denne perioden som frilanser?\",\n" +
            "                        \"undertekst\": null,\n" +
            "                        \"svartype\": \"PROSENT\",\n" +
            "                        \"min\": \"61\",\n" +
            "                        \"max\": \"99\",\n" +
            "                        \"kriterieForVisningAvUndersporsmal\": null,\n" +
            "                        \"svar\": [\n" +
            "                            {\n" +
            "                                \"verdi\": \"65\"\n" +
            "                            }\n" +
            "                        ],\n" +
            "                        \"undersporsmal\": []\n" +
            "                    }\n" +
            "                ]\n" +
            "            },\n" +
            "            {\n" +
            "                \"id\": \"10\",\n" +
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
            "                        \"id\": \"11\",\n" +
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
            "                                \"id\": \"12\",\n" +
            "                                \"tag\": \"INNTEKTSKILDE_ARBEIDSFORHOLD\",\n" +
            "                                \"sporsmalstekst\": \"Arbeidsforhold\",\n" +
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
            "                                        \"id\": \"13\",\n" +
            "                                        \"tag\": \"INNTEKTSKILDE_ARBEIDSFORHOLD_ER_DU_SYKMELDT\",\n" +
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
            "                                \"id\": \"14\",\n" +
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
            "                                        \"id\": \"15\",\n" +
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
            "                                \"id\": \"16\",\n" +
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
            "                                        \"id\": \"17\",\n" +
            "                                        \"tag\": \"INNTEKTSKILDE_FRILANSER_SELVSTENDIG_ER_DU_SYKMELDT\",\n" +
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
            "                                \"id\": \"18\",\n" +
            "                                \"tag\": \"INNTEKTSKILDE_ANNET\",\n" +
            "                                \"sporsmalstekst\": \"Annet\",\n" +
            "                                \"undertekst\": null,\n" +
            "                                \"svartype\": \"CHECKBOX\",\n" +
            "                                \"min\": null,\n" +
            "                                \"max\": null,\n" +
            "                                \"kriterieForVisningAvUndersporsmal\": null,\n" +
            "                                \"svar\": [\n" +
            "                                    {\n" +
            "                                        \"verdi\": \"CHECKED\"\n" +
            "                                    }\n" +
            "                                ],\n" +
            "                                \"undersporsmal\": []\n" +
            "                            }\n" +
            "                        ]\n" +
            "                    }\n" +
            "                ]\n" +
            "            },\n" +
            "            {\n" +
            "                \"id\": \"19\",\n" +
            "                \"tag\": \"UTLAND\",\n" +
            "                \"sporsmalstekst\": \"Har du oppholdt deg utenfor Norge i perioden 20.05.2018 - 28.05.2018?\",\n" +
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
            "                        \"id\": \"20\",\n" +
            "                        \"tag\": \"PERIODER\",\n" +
            "                        \"sporsmalstekst\": \"Når oppholdt du deg utenfor Norge?\",\n" +
            "                        \"undertekst\": null,\n" +
            "                        \"svartype\": \"PERIODER\",\n" +
            "                        \"min\": \"2018-05-20\",\n" +
            "                        \"max\": \"2018-05-28\",\n" +
            "                        \"kriterieForVisningAvUndersporsmal\": null,\n" +
            "                        \"svar\": [\n" +
            "                            {\n" +
            "                                \"verdi\": \"{\\\"fom\\\":\\\"2018-05-22\\\",\\\"tom\\\":\\\"2018-05-24\\\"}\"\n" +
            "                            }\n" +
            "                        ],\n" +
            "                        \"undersporsmal\": []\n" +
            "                    },\n" +
            "                    {\n" +
            "                        \"id\": \"21\",\n" +
            "                        \"tag\": \"UTLANDSOPPHOLD_SOKT_SYKEPENGER\",\n" +
            "                        \"sporsmalstekst\": \"Har du søkt om å beholde sykepenger under dette oppholdet utenfor Norge?\",\n" +
            "                        \"undertekst\": null,\n" +
            "                        \"svartype\": \"JA_NEI\",\n" +
            "                        \"min\": null,\n" +
            "                        \"max\": null,\n" +
            "                        \"kriterieForVisningAvUndersporsmal\": \"NEI\",\n" +
            "                        \"svar\": [\n" +
            "                            {\n" +
            "                                \"verdi\": \"NEI\"\n" +
            "                            }\n" +
            "                        ],\n" +
            "                        \"undersporsmal\": [\n" +
            "                            {\n" +
            "                                \"id\": \"22\",\n" +
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
            "                \"id\": \"23\",\n" +
            "                \"tag\": \"UTDANNING\",\n" +
            "                \"sporsmalstekst\": \"Har du vært under utdanning i løpet av perioden 20.05.2018 - 28.05.2018?\",\n" +
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
            "                        \"id\": \"24\",\n" +
            "                        \"tag\": \"UTDANNING_START\",\n" +
            "                        \"sporsmalstekst\": \"Når startet du på utdanningen?\",\n" +
            "                        \"undertekst\": null,\n" +
            "                        \"svartype\": \"DATO\",\n" +
            "                        \"min\": null,\n" +
            "                        \"max\": \"2018-05-28\",\n" +
            "                        \"kriterieForVisningAvUndersporsmal\": null,\n" +
            "                        \"svar\": [\n" +
            "                            {\n" +
            "                                \"verdi\": \"2018-05-23\"\n" +
            "                            }\n" +
            "                        ],\n" +
            "                        \"undersporsmal\": []\n" +
            "                    },\n" +
            "                    {\n" +
            "                        \"id\": \"25\",\n" +
            "                        \"tag\": \"FULLTIDSSTUDIUM\",\n" +
            "                        \"sporsmalstekst\": \"Er utdanningen et fulltidsstudium?\",\n" +
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
            "                    }\n" +
            "                ]\n" +
            "            },\n" +
            "            {\n" +
            "                \"id\": \"26\",\n" +
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
            "                \"id\": \"27\",\n" +
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

    public static final String beskrivelseSelvstendigMangeSvar = "Søknad om sykepenger fra frilanser for perioden 20.05.2018 - 28.05.2018\n" +
            "\n" +
            "Periode 1:\n" +
            "20.05.2018 - 24.05.2018\n" +
            "Grad: 100\n" +
            "\n" +
            "Periode 2:\n" +
            "25.05.2018 - 28.05.2018\n" +
            "Grad: 40\n" +
            "\n" +
            "Var du tilbake i fullt arbeid som frilanser før sykmeldingsperioden utløp 28.05.2018?\n" +
            "Ja\n" +
            "    Når var du tilbake i arbeid?\n" +
            "    27.05.2018\n" +
            "\n" +
            "I perioden 20.05.2018 - 24.05.2018 var du 100% sykmeldt som frilanser. Jobbet du noe i denne perioden?\n" +
            "Ja\n" +
            "    Hvor mange timer jobber du normalt per uke som frilanser?\n" +
            "    37.5 timer\n" +
            "\n" +
            "    Hvor mye jobbet du totalt i denne perioden som frilanser?\n" +
            "    40 prosent\n" +
            "\n" +
            "I perioden 25.05.2018 - 28.05.2018 skulle du ifølge sykmeldingen jobbe 60% som frilanser. Jobbet du mer enn dette?\n" +
            "Ja\n" +
            "    Hvor mange timer jobber du normalt per uke som frilanser?\n" +
            "    37.5 timer\n" +
            "\n" +
            "    Hvor mye jobbet du totalt i denne perioden som frilanser?\n" +
            "    65 prosent\n" +
            "\n" +
            "Har du andre inntektskilder eller arbeidsforhold?\n" +
            "Ja\n" +
            "    Hvilke andre inntektskilder har du?\n" +
            "        Arbeidsforhold\n" +
            "            Er du sykmeldt fra dette?\n" +
            "            Nei\n" +
            "\n" +
            "        Jordbruker / Fisker / Reindriftsutøver\n" +
            "            Er du sykmeldt fra dette?\n" +
            "            Ja\n" +
            "\n" +
            "        Selvstendig næringsdrivende\n" +
            "            Er du sykmeldt fra dette?\n" +
            "            Ja\n" +
            "\n" +
            "        Annet\n" +
            "\n" +
            "Har du oppholdt deg utenfor Norge i perioden 20.05.2018 - 28.05.2018?\n" +
            "Ja\n" +
            "    Når oppholdt du deg utenfor Norge?\n" +
            "    22.05.2018 - 24.05.2018\n" +
            "\n" +
            "    Har du søkt om å beholde sykepenger under dette oppholdet utenfor Norge?\n" +
            "    Nei\n" +
            "\n" +
            "Har du vært under utdanning i løpet av perioden 20.05.2018 - 28.05.2018?\n" +
            "Ja\n" +
            "    Når startet du på utdanningen?\n" +
            "    23.05.2018\n" +
            "\n" +
            "    Er utdanningen et fulltidsstudium?\n" +
            "    Ja";

    public static final String soknadSelvstendigMedNeisvar = "{\n" +
            "        \"id\": \"daa8b4b5-ece8-4e6d-ab7e-c7354958201a\",\n" +
            "        \"aktorId\": \"aktorId-745463060\",\n" +
            "        \"sykmeldingId\": \"14e78e84-50a5-45bb-9919-191c54f99691\",\n" +
            "        \"soknadstype\": \"SELVSTENDIGE_OG_FRILANSERE\",\n" +
            "        \"status\": \"SENDT\",\n" +
            "        \"fom\": \"2018-05-20\",\n" +
            "        \"tom\": \"2018-05-28\",\n" +
            "        \"opprettet\": \"2018-11-16T00:00:00\",\n" +
            "        \"sendtNav\": \"2018-11-16T00:00:00\",\n" +
            "        \"startSykeforlop\": null,\n" +
            "        \"sykmeldingSkrevet\": null,\n" +
            "        \"arbeidsgiver\": null,\n" +
            "        \"korrigerer\": null,\n" +
            "        \"korrigertAv\": null,\n" +
            "        \"arbeidssituasjon\": \"FRILANSER\",\n" +
            "        \"soknadPerioder\": [\n" +
            "            {\n" +
            "                \"fom\": \"2018-05-20\",\n" +
            "                \"tom\": \"2018-05-24\",\n" +
            "                \"grad\": 100\n" +
            "            },\n" +
            "            {\n" +
            "                \"fom\": \"2018-05-25\",\n" +
            "                \"tom\": \"2018-05-28\",\n" +
            "                \"grad\": 40\n" +
            "            }\n" +
            "        ],\n" +
            "        \"sporsmal\": [\n" +
            "            {\n" +
            "                \"id\": \"1\",\n" +
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
            "                \"id\": \"2\",\n" +
            "                \"tag\": \"TILBAKE_I_ARBEID\",\n" +
            "                \"sporsmalstekst\": \"Var du tilbake i fullt arbeid som frilanser før sykmeldingsperioden utløp 28.05.2018?\",\n" +
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
            "                        \"id\": \"3\",\n" +
            "                        \"tag\": \"TILBAKE_NAR\",\n" +
            "                        \"sporsmalstekst\": \"Når var du tilbake i arbeid?\",\n" +
            "                        \"undertekst\": null,\n" +
            "                        \"svartype\": \"DATO\",\n" +
            "                        \"min\": \"2018-05-20\",\n" +
            "                        \"max\": \"2018-05-29\",\n" +
            "                        \"kriterieForVisningAvUndersporsmal\": null,\n" +
            "                        \"svar\": [],\n" +
            "                        \"undersporsmal\": []\n" +
            "                    }\n" +
            "                ]\n" +
            "            },\n" +
            "            {\n" +
            "                \"id\": \"4\",\n" +
            "                \"tag\": \"JOBBET_DU_100_PROSENT_0\",\n" +
            "                \"sporsmalstekst\": \"I perioden 20.05.2018 - 24.05.2018 var du 100% sykmeldt som frilanser. Jobbet du noe i denne perioden?\",\n" +
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
            "                        \"id\": \"5\",\n" +
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
            "                        \"id\": \"6\",\n" +
            "                        \"tag\": \"HVOR_MYE_HAR_DU_JOBBET_0\",\n" +
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
            "                \"id\": \"7\",\n" +
            "                \"tag\": \"JOBBET_DU_GRADERT_1\",\n" +
            "                \"sporsmalstekst\": \"I perioden 25.05.2018 - 28.05.2018 skulle du ifølge sykmeldingen jobbe 60% som frilanser. Jobbet du mer enn dette?\",\n" +
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
            "                        \"id\": \"8\",\n" +
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
            "                        \"id\": \"9\",\n" +
            "                        \"tag\": \"HVOR_MYE_HAR_DU_JOBBET_1\",\n" +
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
            "                \"id\": \"10\",\n" +
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
            "                        \"id\": \"11\",\n" +
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
            "                                \"id\": \"12\",\n" +
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
            "                                        \"id\": \"13\",\n" +
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
            "                                \"id\": \"14\",\n" +
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
            "                                        \"id\": \"15\",\n" +
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
            "                                \"id\": \"16\",\n" +
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
            "                                        \"id\": \"17\",\n" +
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
            "                                \"id\": \"18\",\n" +
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
            "                \"id\": \"19\",\n" +
            "                \"tag\": \"UTLAND\",\n" +
            "                \"sporsmalstekst\": \"Har du oppholdt deg utenfor Norge i perioden 20.05.2018 - 28.05.2018?\",\n" +
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
            "                        \"id\": \"20\",\n" +
            "                        \"tag\": \"PERIODER\",\n" +
            "                        \"sporsmalstekst\": \"Når oppholdt du deg utenfor Norge?\",\n" +
            "                        \"undertekst\": null,\n" +
            "                        \"svartype\": \"PERIODER\",\n" +
            "                        \"min\": \"2018-05-20\",\n" +
            "                        \"max\": \"2018-05-28\",\n" +
            "                        \"kriterieForVisningAvUndersporsmal\": null,\n" +
            "                        \"svar\": [],\n" +
            "                        \"undersporsmal\": []\n" +
            "                    },\n" +
            "                    {\n" +
            "                        \"id\": \"21\",\n" +
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
            "                                \"id\": \"22\",\n" +
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
            "                \"id\": \"23\",\n" +
            "                \"tag\": \"UTDANNING\",\n" +
            "                \"sporsmalstekst\": \"Har du vært under utdanning i løpet av perioden 20.05.2018 - 28.05.2018?\",\n" +
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
            "                        \"id\": \"24\",\n" +
            "                        \"tag\": \"UTDANNING_START\",\n" +
            "                        \"sporsmalstekst\": \"Når startet du på utdanningen?\",\n" +
            "                        \"undertekst\": null,\n" +
            "                        \"svartype\": \"DATO\",\n" +
            "                        \"min\": null,\n" +
            "                        \"max\": \"2018-05-28\",\n" +
            "                        \"kriterieForVisningAvUndersporsmal\": null,\n" +
            "                        \"svar\": [],\n" +
            "                        \"undersporsmal\": []\n" +
            "                    },\n" +
            "                    {\n" +
            "                        \"id\": \"25\",\n" +
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
            "                \"id\": \"26\",\n" +
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
            "                \"id\": \"27\",\n" +
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

    public static final String beskrivelseSoknadSelvstendigMedNeisvar = "Søknad om sykepenger fra frilanser for perioden 20.05.2018 - 28.05.2018\n" +
            "\n" +
            "Periode 1:\n" +
            "20.05.2018 - 24.05.2018\n" +
            "Grad: 100\n" +
            "\n" +
            "Periode 2:\n" +
            "25.05.2018 - 28.05.2018\n" +
            "Grad: 40\n";

    public static final String soknadUtland = "{\n" +
            "    \"id\": \"95f44b7e-1e58-4b18-b355-511463cde6f9\",\n" +
            "    \"aktorId\": \"aktorId-745463060\",\n" +
            "    \"sykmeldingId\": null,\n" +
            "    \"soknadstype\": \"OPPHOLD_UTLAND\",\n" +
            "    \"status\": \"SENDT\",\n" +
            "    \"fom\": null,\n" +
            "    \"tom\": null,\n" +
            "    \"opprettet\": \"2018-11-16T00:00:00\",\n" +
            "    \"sendtNav\": \"2018-11-16T00:00:00\",\n" +
            "    \"startSykeforlop\": null,\n" +
            "    \"sykmeldingSkrevet\": null,\n" +
            "    \"arbeidsgiver\": null,\n" +
            "    \"korrigerer\": null,\n" +
            "    \"korrigertAv\": null,\n" +
            "    \"arbeidssituasjon\": null,\n" +
            "    \"sporsmal\": [\n" +
            "        {\n" +
            "            \"id\": \"153\",\n" +
            "            \"tag\": \"PERIODEUTLAND\",\n" +
            "            \"sporsmalstekst\": \"Når skal du være utenfor Norge?\",\n" +
            "            \"undertekst\": null,\n" +
            "            \"svartype\": \"PERIODER\",\n" +
            "            \"min\": \"2018-08-16\",\n" +
            "            \"max\": \"2019-05-16\",\n" +
            "            \"kriterieForVisningAvUndersporsmal\": null,\n" +
            "            \"svar\": [\n" +
            "                    {\n" +
            "                        \"verdi\": \"{\\\"fom\\\":\\\"2018-12-10\\\",\\\"tom\\\":\\\"2018-12-18\\\"}\"\n" +
            "                    }\n" +
            "                ],\n" +
            "            \"undersporsmal\": []\n" +
            "        },\n" +
            "        {\n" +
            "            \"id\": \"154\",\n" +
            "            \"tag\": \"LAND\",\n" +
            "            \"sporsmalstekst\": \"Hvor skal du reise?\",\n" +
            "            \"undertekst\": null,\n" +
            "            \"svartype\": \"FRITEKST\",\n" +
            "            \"min\": null,\n" +
            "            \"max\": \"50\",\n" +
            "            \"kriterieForVisningAvUndersporsmal\": null,\n" +
            "            \"svar\": [\n" +
            "                    {\n" +
            "                        \"verdi\": \"Laksefiske\"\n" +
            "                    }\n" +
            "                ],\n" +
            "            \"undersporsmal\": []\n" +
            "        },\n" +
            "        {\n" +
            "            \"id\": \"155\",\n" +
            "            \"tag\": \"ARBEIDSGIVER\",\n" +
            "            \"sporsmalstekst\": \"Har du arbeidsgiver?\",\n" +
            "            \"undertekst\": null,\n" +
            "            \"svartype\": \"JA_NEI\",\n" +
            "            \"min\": null,\n" +
            "            \"max\": null,\n" +
            "            \"kriterieForVisningAvUndersporsmal\": \"JA\",\n" +
            "            \"svar\": [\n" +
            "                    {\n" +
            "                        \"verdi\": \"JA\"\n" +
            "                    }\n" +
            "                ],\n" +
            "            \"undersporsmal\": [\n" +
            "                {\n" +
            "                    \"id\": \"156\",\n" +
            "                    \"tag\": \"SYKMELDINGSGRAD\",\n" +
            "                    \"sporsmalstekst\": \"Er du 100 % sykmeldt?\",\n" +
            "                    \"undertekst\": null,\n" +
            "                    \"svartype\": \"JA_NEI\",\n" +
            "                    \"min\": null,\n" +
            "                    \"max\": null,\n" +
            "                    \"kriterieForVisningAvUndersporsmal\": null,\n" +
            "                    \"svar\": [\n" +
            "                            {\n" +
            "                                \"verdi\": \"JA\"\n" +
            "                            }\n" +
            "                        ],\n" +
            "                    \"undersporsmal\": []\n" +
            "                },\n" +
            "                {\n" +
            "                    \"id\": \"157\",\n" +
            "                    \"tag\": \"FERIE\",\n" +
            "                    \"sporsmalstekst\": \"Har du avtalt med arbeidsgiveren din at du skal ha ferie i hele perioden?\",\n" +
            "                    \"undertekst\": null,\n" +
            "                    \"svartype\": \"JA_NEI\",\n" +
            "                    \"min\": null,\n" +
            "                    \"max\": null,\n" +
            "                    \"kriterieForVisningAvUndersporsmal\": null,\n" +
            "                    \"svar\": [\n" +
            "                            {\n" +
            "                                \"verdi\": \"NEI\"\n" +
            "                            }\n" +
            "                        ],\n" +
            "                    \"undersporsmal\": []\n" +
            "                }\n" +
            "            ]\n" +
            "        },\n" +
            "        {\n" +
            "            \"id\": \"158\",\n" +
            "            \"tag\": \"BEKREFT_OPPLYSNINGER_UTLAND_INFO\",\n" +
            "            \"sporsmalstekst\": \"Før du reiser ber vi deg bekrefte:\",\n" +
            "            \"undertekst\": \"<ul>\\n    <li>Reisen vil ikke gjøre at jeg blir dårligere </li>\\n    <li>Reisen vil ikke gjøre at sykefraværet blir lengre</li>\\n    <li>Reisen vil ikke hindre planlagt behandling eller oppfølging fra NAV</li>\\n</ul>\",\n" +
            "            \"svartype\": \"IKKE_RELEVANT\",\n" +
            "            \"min\": null,\n" +
            "            \"max\": null,\n" +
            "            \"kriterieForVisningAvUndersporsmal\": null,\n" +
            "            \"svar\": [],\n" +
            "            \"undersporsmal\": [\n" +
            "                {\n" +
            "                    \"id\": \"159\",\n" +
            "                    \"tag\": \"BEKREFT_OPPLYSNINGER_UTLAND\",\n" +
            "                    \"sporsmalstekst\": \"Jeg bekrefter de tre punktene ovenfor. Jeg har avklart reisen med legen.\",\n" +
            "                    \"undertekst\": null,\n" +
            "                    \"svartype\": \"CHECKBOX_PANEL\",\n" +
            "                    \"min\": null,\n" +
            "                    \"max\": null,\n" +
            "                    \"kriterieForVisningAvUndersporsmal\": null,\n" +
            "                    \"svar\": [\n" +
            "                            {\n" +
            "                                \"verdi\": \"CHECKED\"\n" +
            "                            }\n" +
            "                        ],\n" +
            "                    \"undersporsmal\": []\n" +
            "                }\n" +
            "            ]\n" +
            "        }\n" +
            "    ]\n" +
            "}";

    public static final String soknadUtlandMedSvartypeLand = "{\n" +
            "    \"id\": \"95f44b7e-1e58-4b18-b355-511463cde6f9\",\n" +
            "    \"aktorId\": \"aktorId-745463060\",\n" +
            "    \"sykmeldingId\": null,\n" +
            "    \"soknadstype\": \"OPPHOLD_UTLAND\",\n" +
            "    \"status\": \"SENDT\",\n" +
            "    \"fom\": null,\n" +
            "    \"tom\": null,\n" +
            "    \"opprettet\": \"2018-11-16T00:00:00\",\n" +
            "    \"sendtNav\": \"2018-11-16T00:00:00\",\n" +
            "    \"startSykeforlop\": null,\n" +
            "    \"sykmeldingSkrevet\": null,\n" +
            "    \"arbeidsgiver\": null,\n" +
            "    \"korrigerer\": null,\n" +
            "    \"korrigertAv\": null,\n" +
            "    \"arbeidssituasjon\": null,\n" +
            "    \"sporsmal\": [\n" +
            "        {\n" +
            "            \"id\": \"153\",\n" +
            "            \"tag\": \"PERIODEUTLAND\",\n" +
            "            \"sporsmalstekst\": \"Når skal du være utenfor Norge?\",\n" +
            "            \"undertekst\": null,\n" +
            "            \"svartype\": \"PERIODER\",\n" +
            "            \"min\": \"2018-08-16\",\n" +
            "            \"max\": \"2019-05-16\",\n" +
            "            \"kriterieForVisningAvUndersporsmal\": null,\n" +
            "            \"svar\": [\n" +
            "                    {\n" +
            "                        \"verdi\": \"{\\\"fom\\\":\\\"2018-12-10\\\",\\\"tom\\\":\\\"2018-12-18\\\"}\"\n" +
            "                    }\n" +
            "                ],\n" +
            "            \"undersporsmal\": []\n" +
            "        },\n" +
            "        {\n" +
            "            \"id\": \"154\",\n" +
            "            \"tag\": \"LAND\",\n" +
            "            \"sporsmalstekst\": \"Hvor skal du reise?\",\n" +
            "            \"undertekst\": null,\n" +
            "            \"svartype\": \"LAND\",\n" +
            "            \"min\": null,\n" +
            "            \"max\": \"50\",\n" +
            "            \"kriterieForVisningAvUndersporsmal\": null,\n" +
            "            \"svar\": [\n" +
            "                    {\n" +
            "                        \"verdi\": \"Sverige\"\n" +
            "                    },\n" +
            "                    {\n" +
            "                        \"verdi\": \"Danmark\"\n" +
            "                    }\n" +
            "                ],\n" +
            "            \"undersporsmal\": []\n" +
            "        },\n" +
            "        {\n" +
            "            \"id\": \"155\",\n" +
            "            \"tag\": \"ARBEIDSGIVER\",\n" +
            "            \"sporsmalstekst\": \"Har du arbeidsgiver?\",\n" +
            "            \"undertekst\": null,\n" +
            "            \"svartype\": \"JA_NEI\",\n" +
            "            \"min\": null,\n" +
            "            \"max\": null,\n" +
            "            \"kriterieForVisningAvUndersporsmal\": \"JA\",\n" +
            "            \"svar\": [\n" +
            "                    {\n" +
            "                        \"verdi\": \"JA\"\n" +
            "                    }\n" +
            "                ],\n" +
            "            \"undersporsmal\": [\n" +
            "                {\n" +
            "                    \"id\": \"156\",\n" +
            "                    \"tag\": \"SYKMELDINGSGRAD\",\n" +
            "                    \"sporsmalstekst\": \"Er du 100 % sykmeldt?\",\n" +
            "                    \"undertekst\": null,\n" +
            "                    \"svartype\": \"JA_NEI\",\n" +
            "                    \"min\": null,\n" +
            "                    \"max\": null,\n" +
            "                    \"kriterieForVisningAvUndersporsmal\": null,\n" +
            "                    \"svar\": [\n" +
            "                            {\n" +
            "                                \"verdi\": \"JA\"\n" +
            "                            }\n" +
            "                        ],\n" +
            "                    \"undersporsmal\": []\n" +
            "                },\n" +
            "                {\n" +
            "                    \"id\": \"157\",\n" +
            "                    \"tag\": \"FERIE\",\n" +
            "                    \"sporsmalstekst\": \"Har du avtalt med arbeidsgiveren din at du skal ha ferie i hele perioden?\",\n" +
            "                    \"undertekst\": null,\n" +
            "                    \"svartype\": \"JA_NEI\",\n" +
            "                    \"min\": null,\n" +
            "                    \"max\": null,\n" +
            "                    \"kriterieForVisningAvUndersporsmal\": null,\n" +
            "                    \"svar\": [\n" +
            "                            {\n" +
            "                                \"verdi\": \"NEI\"\n" +
            "                            }\n" +
            "                        ],\n" +
            "                    \"undersporsmal\": []\n" +
            "                }\n" +
            "            ]\n" +
            "        },\n" +
            "        {\n" +
            "            \"id\": \"158\",\n" +
            "            \"tag\": \"BEKREFT_OPPLYSNINGER_UTLAND_INFO\",\n" +
            "            \"sporsmalstekst\": \"Før du reiser ber vi deg bekrefte:\",\n" +
            "            \"undertekst\": \"<ul>\\n    <li>Reisen vil ikke gjøre at jeg blir dårligere </li>\\n    <li>Reisen vil ikke gjøre at sykefraværet blir lengre</li>\\n    <li>Reisen vil ikke hindre planlagt behandling eller oppfølging fra NAV</li>\\n</ul>\",\n" +
            "            \"svartype\": \"IKKE_RELEVANT\",\n" +
            "            \"min\": null,\n" +
            "            \"max\": null,\n" +
            "            \"kriterieForVisningAvUndersporsmal\": null,\n" +
            "            \"svar\": [],\n" +
            "            \"undersporsmal\": [\n" +
            "                {\n" +
            "                    \"id\": \"159\",\n" +
            "                    \"tag\": \"BEKREFT_OPPLYSNINGER_UTLAND\",\n" +
            "                    \"sporsmalstekst\": \"Jeg bekrefter de tre punktene ovenfor. Jeg har avklart reisen med legen.\",\n" +
            "                    \"undertekst\": null,\n" +
            "                    \"svartype\": \"CHECKBOX_PANEL\",\n" +
            "                    \"min\": null,\n" +
            "                    \"max\": null,\n" +
            "                    \"kriterieForVisningAvUndersporsmal\": null,\n" +
            "                    \"svar\": [\n" +
            "                            {\n" +
            "                                \"verdi\": \"CHECKED\"\n" +
            "                            }\n" +
            "                        ],\n" +
            "                    \"undersporsmal\": []\n" +
            "                }\n" +
            "            ]\n" +
            "        }\n" +
            "    ]\n" +
            "}";

    public static final String beskrivelseUtland = "Søknad om å beholde sykepenger i utlandet\n" +
            "\n" +
            "Når skal du være utenfor Norge?\n" +
            "10.12.2018 - 18.12.2018\n" +
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
            "    Nei";

    public static final String beskrivelseUtlandMedSvartypeLand = "Søknad om å beholde sykepenger i utlandet\n" +
            "\n" +
            "Når skal du være utenfor Norge?\n" +
            "10.12.2018 - 18.12.2018\n" +
            "\n" +
            "Hvor skal du reise?\n" +
            "- Sverige\n" +
            "- Danmark\n" +
            "\n" +
            "Har du arbeidsgiver?\n" +
            "Ja\n" +
            "    Er du 100 % sykmeldt?\n" +
            "    Ja\n" +
            "\n" +
            "    Har du avtalt med arbeidsgiveren din at du skal ha ferie i hele perioden?\n" +
            "    Nei";

    public static final String soknadArbeidstakerMedNeisvar = "{\n" +
            "        \"id\": \"27010c11-9200-44c7-b44c-6428bc6762d7\",\n" +
            "        \"aktorId\": \"aktorId-745463060\",\n" +
            "        \"sykmeldingId\": \"14e78e84-50a5-45bb-9919-191c54f99691\",\n" +
            "        \"soknadstype\": \"ARBEIDSTAKERE\",\n" +
            "        \"status\": \"SENDT\",\n" +
            "        \"fom\": \"2018-10-16\",\n" +
            "        \"tom\": \"2018-10-24\",\n" +
            "        \"opprettet\": \"2018-11-16T00:00:00\",\n" +
            "        \"sendtNav\": \"2018-11-16T00:00:00\",\n" +
            "        \"startSykeforlop\": \"2018-10-16\",\n" +
            "        \"sykmeldingSkrevet\": null,\n" +
            "        \"arbeidsgiver\": \"ARBEIDSGIVER A/S\",\n" +
            "        \"korrigerer\": null,\n" +
            "        \"korrigertAv\": null,\n" +
            "        \"arbeidssituasjon\": \"ARBEIDSTAKER\",\n" +
            "        \"soknadPerioder\": [\n" +
            "            {\n" +
            "                \"fom\": \"2018-10-16\",\n" +
            "                \"tom\": \"2018-10-20\",\n" +
            "                \"grad\": 100\n" +
            "            },\n" +
            "            {\n" +
            "                \"fom\": \"2018-10-21\",\n" +
            "                \"tom\": \"2018-10-24\",\n" +
            "                \"grad\": 40\n" +
            "            }\n" +
            "        ],\n" +
            "        \"sporsmal\": [\n" +
            "            {\n" +
            "                \"id\": \"55\",\n" +
            "                \"tag\": \"ANSVARSERKLARING\",\n" +
            "                \"sporsmalstekst\": \"Jeg vet at dersom jeg gir uriktige opplysninger, eller holder tilbake opplysninger som har betydning for min rett til sykepenger, kan pengene holdes tilbake eller kreves tilbake, og/eller det kan medføre straffeansvar. Jeg er også klar over at jeg må melde fra til NAV dersom jeg i sykmeldingsperioden satt i varetekt, sonet straff eller var under forvaring.\",\n" +
            "                \"undertekst\": null,\n" +
            "                \"svartype\": \"CHECKBOX_PANEL\",\n" +
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
            "                \"id\": \"56\",\n" +
            "                \"tag\": \"EGENMELDINGER\",\n" +
            "                \"sporsmalstekst\": \"Vi har registrert at du ble sykmeldt tirsdag 16. oktober 2018. Brukte du egenmeldinger og/eller var du sykmeldt i perioden 30. september - 15. oktober 2018?\",\n" +
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
            "                        \"id\": \"57\",\n" +
            "                        \"tag\": \"EGENMELDINGER_NAR\",\n" +
            "                        \"sporsmalstekst\": \"Hvilke dager før 16. oktober 2018 var du borte fra jobb?\",\n" +
            "                        \"undertekst\": null,\n" +
            "                        \"svartype\": \"PERIODER\",\n" +
            "                        \"min\": \"2018-04-16\",\n" +
            "                        \"max\": \"2018-10-15\",\n" +
            "                        \"kriterieForVisningAvUndersporsmal\": null,\n" +
            "                        \"svar\": [],\n" +
            "                        \"undersporsmal\": []\n" +
            "                    }\n" +
            "                ]\n" +
            "            },\n" +
            "            {\n" +
            "                \"id\": \"58\",\n" +
            "                \"tag\": \"TILBAKE_I_ARBEID\",\n" +
            "                \"sporsmalstekst\": \"Var du tilbake i fullt arbeid hos ARBEIDSGIVER A/S før 25. oktober 2018?\",\n" +
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
            "                        \"id\": \"59\",\n" +
            "                        \"tag\": \"TILBAKE_NAR\",\n" +
            "                        \"sporsmalstekst\": \"Fra hvilken dato ble arbeidet gjenopptatt?\",\n" +
            "                        \"undertekst\": null,\n" +
            "                        \"svartype\": \"DATO\",\n" +
            "                        \"min\": \"2018-10-16\",\n" +
            "                        \"max\": \"2018-10-24\",\n" +
            "                        \"kriterieForVisningAvUndersporsmal\": null,\n" +
            "                        \"svar\": [],\n" +
            "                        \"undersporsmal\": []\n" +
            "                    }\n" +
            "                ]\n" +
            "            },\n" +
            "            {\n" +
            "                \"id\": \"60\",\n" +
            "                \"tag\": \"JOBBET_DU_100_PROSENT_0\",\n" +
            "                \"sporsmalstekst\": \"I perioden 16. - 20. oktober 2018 var du 100 % sykmeldt fra ARBEIDSGIVER A/S. Jobbet du noe i denne perioden?\",\n" +
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
            "                        \"id\": \"61\",\n" +
            "                        \"tag\": \"HVOR_MANGE_TIMER_PER_UKE_0\",\n" +
            "                        \"sporsmalstekst\": \"Hvor mange timer jobbet du per uke før du ble sykmeldt?\",\n" +
            "                        \"undertekst\": \"timer per uke\",\n" +
            "                        \"svartype\": \"TALL\",\n" +
            "                        \"min\": \"1\",\n" +
            "                        \"max\": \"150\",\n" +
            "                        \"kriterieForVisningAvUndersporsmal\": null,\n" +
            "                        \"svar\": [],\n" +
            "                        \"undersporsmal\": []\n" +
            "                    },\n" +
            "                    {\n" +
            "                        \"id\": \"62\",\n" +
            "                        \"tag\": \"HVOR_MYE_HAR_DU_JOBBET_0\",\n" +
            "                        \"sporsmalstekst\": \"Hvor mye jobbet du totalt 16. - 20. oktober 2018 hos ARBEIDSGIVER A/S?\",\n" +
            "                        \"undertekst\": null,\n" +
            "                        \"svartype\": \"RADIO_GRUPPE_TIMER_PROSENT\",\n" +
            "                        \"min\": null,\n" +
            "                        \"max\": null,\n" +
            "                        \"kriterieForVisningAvUndersporsmal\": null,\n" +
            "                        \"svar\": [],\n" +
            "                        \"undersporsmal\": [\n" +
            "                            {\n" +
            "                                \"id\": \"63\",\n" +
            "                                \"tag\": \"HVOR_MYE_PROSENT_0\",\n" +
            "                                \"sporsmalstekst\": \"prosent\",\n" +
            "                                \"undertekst\": null,\n" +
            "                                \"svartype\": \"RADIO\",\n" +
            "                                \"min\": null,\n" +
            "                                \"max\": null,\n" +
            "                                \"kriterieForVisningAvUndersporsmal\": \"CHECKED\",\n" +
            "                                \"svar\": [],\n" +
            "                                \"undersporsmal\": [\n" +
            "                                    {\n" +
            "                                        \"id\": \"64\",\n" +
            "                                        \"tag\": \"HVOR_MYE_PROSENT_VERDI_0\",\n" +
            "                                        \"sporsmalstekst\": null,\n" +
            "                                        \"undertekst\": \"prosent\",\n" +
            "                                        \"svartype\": \"TALL\",\n" +
            "                                        \"min\": \"1\",\n" +
            "                                        \"max\": \"99\",\n" +
            "                                        \"kriterieForVisningAvUndersporsmal\": null,\n" +
            "                                        \"svar\": [],\n" +
            "                                        \"undersporsmal\": []\n" +
            "                                    }\n" +
            "                                ]\n" +
            "                            },\n" +
            "                            {\n" +
            "                                \"id\": \"65\",\n" +
            "                                \"tag\": \"HVOR_MYE_TIMER_0\",\n" +
            "                                \"sporsmalstekst\": \"timer\",\n" +
            "                                \"undertekst\": null,\n" +
            "                                \"svartype\": \"RADIO\",\n" +
            "                                \"min\": null,\n" +
            "                                \"max\": null,\n" +
            "                                \"kriterieForVisningAvUndersporsmal\": \"CHECKED\",\n" +
            "                                \"svar\": [],\n" +
            "                                \"undersporsmal\": [\n" +
            "                                    {\n" +
            "                                        \"id\": \"66\",\n" +
            "                                        \"tag\": \"HVOR_MYE_TIMER_VERDI_0\",\n" +
            "                                        \"sporsmalstekst\": null,\n" +
            "                                        \"undertekst\": \"timer totalt\",\n" +
            "                                        \"svartype\": \"TALL\",\n" +
            "                                        \"min\": \"1\",\n" +
            "                                        \"max\": \"107\",\n" +
            "                                        \"kriterieForVisningAvUndersporsmal\": null,\n" +
            "                                        \"svar\": [],\n" +
            "                                        \"undersporsmal\": []\n" +
            "                                    }\n" +
            "                                ]\n" +
            "                            }\n" +
            "                        ]\n" +
            "                    }\n" +
            "                ]\n" +
            "            },\n" +
            "            {\n" +
            "                \"id\": \"67\",\n" +
            "                \"tag\": \"JOBBET_DU_GRADERT_1\",\n" +
            "                \"sporsmalstekst\": \"I perioden 21. - 24. oktober 2018 skulle du jobbe 60 % av ditt normale arbeid hos ARBEIDSGIVER A/S. Jobbet du mer enn dette?\",\n" +
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
            "                        \"id\": \"68\",\n" +
            "                        \"tag\": \"HVOR_MANGE_TIMER_PER_UKE_1\",\n" +
            "                        \"sporsmalstekst\": \"Hvor mange timer jobbet du per uke før du ble sykmeldt?\",\n" +
            "                        \"undertekst\": \"timer per uke\",\n" +
            "                        \"svartype\": \"TALL\",\n" +
            "                        \"min\": \"1\",\n" +
            "                        \"max\": \"150\",\n" +
            "                        \"kriterieForVisningAvUndersporsmal\": null,\n" +
            "                        \"svar\": [],\n" +
            "                        \"undersporsmal\": []\n" +
            "                    },\n" +
            "                    {\n" +
            "                        \"id\": \"69\",\n" +
            "                        \"tag\": \"HVOR_MYE_HAR_DU_JOBBET_1\",\n" +
            "                        \"sporsmalstekst\": \"Hvor mye jobbet du totalt 21. - 24. oktober 2018 hos ARBEIDSGIVER A/S?\",\n" +
            "                        \"undertekst\": null,\n" +
            "                        \"svartype\": \"RADIO_GRUPPE_TIMER_PROSENT\",\n" +
            "                        \"min\": null,\n" +
            "                        \"max\": null,\n" +
            "                        \"kriterieForVisningAvUndersporsmal\": null,\n" +
            "                        \"svar\": [],\n" +
            "                        \"undersporsmal\": [\n" +
            "                            {\n" +
            "                                \"id\": \"70\",\n" +
            "                                \"tag\": \"HVOR_MYE_PROSENT_1\",\n" +
            "                                \"sporsmalstekst\": \"prosent\",\n" +
            "                                \"undertekst\": null,\n" +
            "                                \"svartype\": \"RADIO\",\n" +
            "                                \"min\": null,\n" +
            "                                \"max\": null,\n" +
            "                                \"kriterieForVisningAvUndersporsmal\": \"CHECKED\",\n" +
            "                                \"svar\": [],\n" +
            "                                \"undersporsmal\": [\n" +
            "                                    {\n" +
            "                                        \"id\": \"71\",\n" +
            "                                        \"tag\": \"HVOR_MYE_PROSENT_VERDI_1\",\n" +
            "                                        \"sporsmalstekst\": null,\n" +
            "                                        \"undertekst\": \"prosent\",\n" +
            "                                        \"svartype\": \"TALL\",\n" +
            "                                        \"min\": \"61\",\n" +
            "                                        \"max\": \"99\",\n" +
            "                                        \"kriterieForVisningAvUndersporsmal\": null,\n" +
            "                                        \"svar\": [],\n" +
            "                                        \"undersporsmal\": []\n" +
            "                                    }\n" +
            "                                ]\n" +
            "                            },\n" +
            "                            {\n" +
            "                                \"id\": \"72\",\n" +
            "                                \"tag\": \"HVOR_MYE_TIMER_1\",\n" +
            "                                \"sporsmalstekst\": \"timer\",\n" +
            "                                \"undertekst\": null,\n" +
            "                                \"svartype\": \"RADIO\",\n" +
            "                                \"min\": null,\n" +
            "                                \"max\": null,\n" +
            "                                \"kriterieForVisningAvUndersporsmal\": \"CHECKED\",\n" +
            "                                \"svar\": [],\n" +
            "                                \"undersporsmal\": [\n" +
            "                                    {\n" +
            "                                        \"id\": \"73\",\n" +
            "                                        \"tag\": \"HVOR_MYE_TIMER_VERDI_1\",\n" +
            "                                        \"sporsmalstekst\": null,\n" +
            "                                        \"undertekst\": \"timer totalt\",\n" +
            "                                        \"svartype\": \"TALL\",\n" +
            "                                        \"min\": \"1\",\n" +
            "                                        \"max\": \"86\",\n" +
            "                                        \"kriterieForVisningAvUndersporsmal\": null,\n" +
            "                                        \"svar\": [],\n" +
            "                                        \"undersporsmal\": []\n" +
            "                                    }\n" +
            "                                ]\n" +
            "                            }\n" +
            "                        ]\n" +
            "                    }\n" +
            "                ]\n" +
            "            },\n" +
            "            {\n" +
            "                \"id\": \"74\",\n" +
            "                \"tag\": \"FERIE_PERMISJON_UTLAND\",\n" +
            "                \"sporsmalstekst\": \"Har du hatt ferie, permisjon eller oppholdt deg utenfor Norge i perioden 16. - 24. oktober 2018?\",\n" +
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
            "                        \"id\": \"75\",\n" +
            "                        \"tag\": \"FERIE_PERMISJON_UTLAND_HVA\",\n" +
            "                        \"sporsmalstekst\": \"Kryss av alt som gjelder deg:\",\n" +
            "                        \"undertekst\": null,\n" +
            "                        \"svartype\": \"CHECKBOX_GRUPPE\",\n" +
            "                        \"min\": null,\n" +
            "                        \"max\": null,\n" +
            "                        \"kriterieForVisningAvUndersporsmal\": null,\n" +
            "                        \"svar\": [],\n" +
            "                        \"undersporsmal\": [\n" +
            "                            {\n" +
            "                                \"id\": \"76\",\n" +
            "                                \"tag\": \"FERIE\",\n" +
            "                                \"sporsmalstekst\": \"Jeg tok ut ferie\",\n" +
            "                                \"undertekst\": null,\n" +
            "                                \"svartype\": \"CHECKBOX\",\n" +
            "                                \"min\": null,\n" +
            "                                \"max\": null,\n" +
            "                                \"kriterieForVisningAvUndersporsmal\": \"CHECKED\",\n" +
            "                                \"svar\": [],\n" +
            "                                \"undersporsmal\": [\n" +
            "                                    {\n" +
            "                                        \"id\": \"77\",\n" +
            "                                        \"tag\": \"FERIE_NAR\",\n" +
            "                                        \"sporsmalstekst\": null,\n" +
            "                                        \"undertekst\": null,\n" +
            "                                        \"svartype\": \"PERIODER\",\n" +
            "                                        \"min\": \"2018-10-16\",\n" +
            "                                        \"max\": \"2018-10-24\",\n" +
            "                                        \"kriterieForVisningAvUndersporsmal\": null,\n" +
            "                                        \"svar\": [],\n" +
            "                                        \"undersporsmal\": []\n" +
            "                                    }\n" +
            "                                ]\n" +
            "                            },\n" +
            "                            {\n" +
            "                                \"id\": \"78\",\n" +
            "                                \"tag\": \"PERMISJON\",\n" +
            "                                \"sporsmalstekst\": \"Jeg hadde permisjon\",\n" +
            "                                \"undertekst\": null,\n" +
            "                                \"svartype\": \"CHECKBOX\",\n" +
            "                                \"min\": null,\n" +
            "                                \"max\": null,\n" +
            "                                \"kriterieForVisningAvUndersporsmal\": \"CHECKED\",\n" +
            "                                \"svar\": [],\n" +
            "                                \"undersporsmal\": [\n" +
            "                                    {\n" +
            "                                        \"id\": \"79\",\n" +
            "                                        \"tag\": \"PERMISJON_NAR\",\n" +
            "                                        \"sporsmalstekst\": null,\n" +
            "                                        \"undertekst\": null,\n" +
            "                                        \"svartype\": \"PERIODER\",\n" +
            "                                        \"min\": \"2018-10-16\",\n" +
            "                                        \"max\": \"2018-10-24\",\n" +
            "                                        \"kriterieForVisningAvUndersporsmal\": null,\n" +
            "                                        \"svar\": [],\n" +
            "                                        \"undersporsmal\": []\n" +
            "                                    }\n" +
            "                                ]\n" +
            "                            },\n" +
            "                            {\n" +
            "                                \"id\": \"80\",\n" +
            "                                \"tag\": \"UTLAND\",\n" +
            "                                \"sporsmalstekst\": \"Jeg var utenfor Norge\",\n" +
            "                                \"undertekst\": null,\n" +
            "                                \"svartype\": \"CHECKBOX\",\n" +
            "                                \"min\": null,\n" +
            "                                \"max\": null,\n" +
            "                                \"kriterieForVisningAvUndersporsmal\": \"CHECKED\",\n" +
            "                                \"svar\": [],\n" +
            "                                \"undersporsmal\": [\n" +
            "                                    {\n" +
            "                                        \"id\": \"81\",\n" +
            "                                        \"tag\": \"UTLAND_NAR\",\n" +
            "                                        \"sporsmalstekst\": null,\n" +
            "                                        \"undertekst\": null,\n" +
            "                                        \"svartype\": \"PERIODER\",\n" +
            "                                        \"min\": \"2018-10-16\",\n" +
            "                                        \"max\": \"2018-10-24\",\n" +
            "                                        \"kriterieForVisningAvUndersporsmal\": null,\n" +
            "                                        \"svar\": [],\n" +
            "                                        \"undersporsmal\": []\n" +
            "                                    }\n" +
            "                                ]\n" +
            "                            }\n" +
            "                        ]\n" +
            "                    }\n" +
            "                ]\n" +
            "            },\n" +
            "            {\n" +
            "                \"id\": \"82\",\n" +
            "                \"tag\": \"ANDRE_INNTEKTSKILDER\",\n" +
            "                \"sporsmalstekst\": \"Har du andre inntektskilder, eller jobber du for andre enn ARBEIDSGIVER A/S?\",\n" +
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
            "                        \"id\": \"83\",\n" +
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
            "                                \"id\": \"84\",\n" +
            "                                \"tag\": \"INNTEKTSKILDE_ANDRE_ARBEIDSFORHOLD\",\n" +
            "                                \"sporsmalstekst\": \"Andre arbeidsforhold\",\n" +
            "                                \"undertekst\": null,\n" +
            "                                \"svartype\": \"CHECKBOX\",\n" +
            "                                \"min\": null,\n" +
            "                                \"max\": null,\n" +
            "                                \"kriterieForVisningAvUndersporsmal\": \"CHECKED\",\n" +
            "                                \"svar\": [],\n" +
            "                                \"undersporsmal\": [\n" +
            "                                    {\n" +
            "                                        \"id\": \"85\",\n" +
            "                                        \"tag\": \"INNTEKTSKILDE_ANDRE_ARBEIDSFORHOLD_ER_DU_SYKMELDT\",\n" +
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
            "                                \"id\": \"86\",\n" +
            "                                \"tag\": \"INNTEKTSKILDE_SELVSTENDIG\",\n" +
            "                                \"sporsmalstekst\": \"Selvstendig næringsdrivende\",\n" +
            "                                \"undertekst\": null,\n" +
            "                                \"svartype\": \"CHECKBOX\",\n" +
            "                                \"min\": null,\n" +
            "                                \"max\": null,\n" +
            "                                \"kriterieForVisningAvUndersporsmal\": \"CHECKED\",\n" +
            "                                \"svar\": [],\n" +
            "                                \"undersporsmal\": [\n" +
            "                                    {\n" +
            "                                        \"id\": \"87\",\n" +
            "                                        \"tag\": \"INNTEKTSKILDE_SELVSTENDIG_ER_DU_SYKMELDT\",\n" +
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
            "                                \"id\": \"88\",\n" +
            "                                \"tag\": \"INNTEKTSKILDE_SELVSTENDIG_DAGMAMMA\",\n" +
            "                                \"sporsmalstekst\": \"Selvstendig næringsdrivende dagmamma\",\n" +
            "                                \"undertekst\": null,\n" +
            "                                \"svartype\": \"CHECKBOX\",\n" +
            "                                \"min\": null,\n" +
            "                                \"max\": null,\n" +
            "                                \"kriterieForVisningAvUndersporsmal\": \"CHECKED\",\n" +
            "                                \"svar\": [],\n" +
            "                                \"undersporsmal\": [\n" +
            "                                    {\n" +
            "                                        \"id\": \"89\",\n" +
            "                                        \"tag\": \"INNTEKTSKILDE_SELVSTENDIG_DAGMAMMA_ER_DU_SYKMELDT\",\n" +
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
            "                                \"id\": \"90\",\n" +
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
            "                                        \"id\": \"91\",\n" +
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
            "                                \"id\": \"92\",\n" +
            "                                \"tag\": \"INNTEKTSKILDE_FRILANSER\",\n" +
            "                                \"sporsmalstekst\": \"Frilanser\",\n" +
            "                                \"undertekst\": null,\n" +
            "                                \"svartype\": \"CHECKBOX\",\n" +
            "                                \"min\": null,\n" +
            "                                \"max\": null,\n" +
            "                                \"kriterieForVisningAvUndersporsmal\": \"CHECKED\",\n" +
            "                                \"svar\": [],\n" +
            "                                \"undersporsmal\": [\n" +
            "                                    {\n" +
            "                                        \"id\": \"93\",\n" +
            "                                        \"tag\": \"INNTEKTSKILDE_FRILANSER_ER_DU_SYKMELDT\",\n" +
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
            "                                \"id\": \"94\",\n" +
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
            "                \"id\": \"95\",\n" +
            "                \"tag\": \"UTDANNING\",\n" +
            "                \"sporsmalstekst\": \"Har du vært under utdanning i løpet av perioden 16. - 24. oktober 2018?\",\n" +
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
            "                        \"id\": \"96\",\n" +
            "                        \"tag\": \"UTDANNING_START\",\n" +
            "                        \"sporsmalstekst\": \"Når startet du på utdanningen?\",\n" +
            "                        \"undertekst\": null,\n" +
            "                        \"svartype\": \"DATO\",\n" +
            "                        \"min\": null,\n" +
            "                        \"max\": \"2018-10-24\",\n" +
            "                        \"kriterieForVisningAvUndersporsmal\": null,\n" +
            "                        \"svar\": [],\n" +
            "                        \"undersporsmal\": []\n" +
            "                    },\n" +
            "                    {\n" +
            "                        \"id\": \"97\",\n" +
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
            "                \"id\": \"98\",\n" +
            "                \"tag\": \"VAER_KLAR_OVER_AT\",\n" +
            "                \"sporsmalstekst\": \"Vær klar over at:\",\n" +
            "                \"undertekst\": \"<ul><li>rett til sykepenger forutsetter at du er borte fra arbeid på grunn av egen sykdom. Sosiale eller økonomiske problemer gir ikke rett til sykepenger</li><li>du kan miste retten til sykepenger hvis du uten rimelig grunn nekter å opplyse om egen funksjonsevne eller nekter å ta imot tilbud om behandling og/eller tilrettelegging</li><li>sykepenger utbetales i maksimum 52 uker, også for gradert (delvis) sykmelding</li><li>fristen for å søke sykepenger er som hovedregel 3 måneder</li></ul>\",\n" +
            "                \"svartype\": \"IKKE_RELEVANT\",\n" +
            "                \"min\": null,\n" +
            "                \"max\": null,\n" +
            "                \"kriterieForVisningAvUndersporsmal\": null,\n" +
            "                \"svar\": [],\n" +
            "                \"undersporsmal\": []\n" +
            "            },\n" +
            "            {\n" +
            "                \"id\": \"99\",\n" +
            "                \"tag\": \"BEKREFT_OPPLYSNINGER\",\n" +
            "                \"sporsmalstekst\": \"Jeg har lest all informasjonen jeg har fått i søknaden og bekrefter at opplysningene jeg har gitt er korrekte.\",\n" +
            "                \"undertekst\": null,\n" +
            "                \"svartype\": \"CHECKBOX_PANEL\",\n" +
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
            "                \"id\": \"100\",\n" +
            "                \"tag\": \"BETALER_ARBEIDSGIVER\",\n" +
            "                \"sporsmalstekst\": \"Betaler arbeidsgiveren lønnen din når du er syk?\",\n" +
            "                \"undertekst\": null,\n" +
            "                \"svartype\": \"RADIO_GRUPPE\",\n" +
            "                \"min\": null,\n" +
            "                \"max\": null,\n" +
            "                \"kriterieForVisningAvUndersporsmal\": null,\n" +
            "                \"svar\": [],\n" +
            "                \"undersporsmal\": [\n" +
            "                    {\n" +
            "                        \"id\": \"101\",\n" +
            "                        \"tag\": \"BETALER_ARBEIDSGIVER_JA\",\n" +
            "                        \"sporsmalstekst\": \"Ja\",\n" +
            "                        \"undertekst\": \"Arbeidsgiveren din mottar kopi av søknaden du sender til NAV.\",\n" +
            "                        \"svartype\": \"RADIO\",\n" +
            "                        \"min\": null,\n" +
            "                        \"max\": null,\n" +
            "                        \"kriterieForVisningAvUndersporsmal\": null,\n" +
            "                        \"svar\": [],\n" +
            "                        \"undersporsmal\": []\n" +
            "                    },\n" +
            "                    {\n" +
            "                        \"id\": \"102\",\n" +
            "                        \"tag\": \"BETALER_ARBEIDSGIVER_NEI\",\n" +
            "                        \"sporsmalstekst\": \"Nei\",\n" +
            "                        \"undertekst\": \"Søknaden sendes til NAV. Arbeidsgiveren din får ikke kopi.\",\n" +
            "                        \"svartype\": \"RADIO\",\n" +
            "                        \"min\": null,\n" +
            "                        \"max\": null,\n" +
            "                        \"kriterieForVisningAvUndersporsmal\": null,\n" +
            "                        \"svar\": [],\n" +
            "                        \"undersporsmal\": []\n" +
            "                    },\n" +
            "                    {\n" +
            "                        \"id\": \"103\",\n" +
            "                        \"tag\": \"BETALER_ARBEIDSGIVER_VET_IKKE\",\n" +
            "                        \"sporsmalstekst\": \"Vet ikke\",\n" +
            "                        \"undertekst\": \"Siden du ikke vet svaret på dette spørsmålet, vil arbeidsgiveren din motta en kopi av søknaden du sender til NAV.\",\n" +
            "                        \"svartype\": \"RADIO\",\n" +
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

    public static final String beskrivelseArbeidstakerMedNeisvar = "Søknad om sykepenger for perioden 16.10.2018 - 24.10.2018\n" +
            "\n" +
            "Arbeidsgiver: ARBEIDSGIVER A/S" +
            "\n" +
            "\n" +
            "Periode 1:\n" +
            "16.10.2018 - 20.10.2018\n" +
            "Grad: 100\n" +
            "\n" +
            "Periode 2:\n" +
            "21.10.2018 - 24.10.2018\n" +
            "Grad: 40\n" +
            "\n" +
            "Betaler arbeidsgiveren lønnen din når du er syk?\n" +
            "Vet ikke";

    public static final String beskrivelseArbeidstakerMedNeisvarKorrigert = "Søknad om sykepenger for perioden 16.10.2018 - 24.10.2018 KORRIGERING\n" +
            "\n" +
            "Arbeidsgiver: ARBEIDSGIVER A/S" +
            "\n" +
            "\n" +
            "Periode 1:\n" +
            "16.10.2018 - 20.10.2018\n" +
            "Grad: 100\n" +
            "\n" +
            "Periode 2:\n" +
            "21.10.2018 - 24.10.2018\n" +
            "Grad: 40\n" +
            "\n" +
            "Betaler arbeidsgiveren lønnen din når du er syk?\n" +
            "Vet ikke";


    public static final String soknadArbeidstakerMangeSvar = "{\n" +
            "        \"id\": \"27010c11-9200-44c7-b44c-6428bc6762d7\",\n" +
            "        \"aktorId\": \"aktorId-745463060\",\n" +
            "        \"sykmeldingId\": \"14e78e84-50a5-45bb-9919-191c54f99691\",\n" +
            "        \"soknadstype\": \"ARBEIDSTAKERE\",\n" +
            "        \"status\": \"SENDT\",\n" +
            "        \"fom\": \"2018-10-16\",\n" +
            "        \"tom\": \"2018-10-24\",\n" +
            "        \"opprettet\": \"2018-11-16T00:00:00\",\n" +
            "        \"sendtNav\": \"2018-11-16T00:00:00\",\n" +
            "        \"startSykeforlop\": \"2018-10-16\",\n" +
            "        \"sykmeldingSkrevet\": null,\n" +
            "        \"arbeidsgiver\": \"ARBEIDSGIVER A/S\",\n" +
            "        \"korrigerer\": null,\n" +
            "        \"korrigertAv\": null,\n" +
            "        \"arbeidssituasjon\": \"ARBEIDSTAKER\",\n" +
            "        \"soknadPerioder\": [\n" +
            "            {\n" +
            "                \"fom\": \"2018-10-16\",\n" +
            "                \"tom\": \"2018-10-20\",\n" +
            "                \"grad\": 100,\n" +
            "                \"faktiskGrad\": 79,\n" +
            "                \"avtaltTimer\": 37.5,\n" +
            "                \"faktiskTimer\": null\n" +
            "            },\n" +
            "            {\n" +
            "                \"fom\": \"2018-10-21\",\n" +
            "                \"tom\": \"2018-10-24\",\n" +
            "                \"grad\": 40,\n" +
            "                \"faktiskGrad\": 440,\n" +
            "                \"avtaltTimer\": 37.5,\n" +
            "                \"faktiskTimer\": 66\n" +
            "            }\n" +
            "        ],\n" +
            "        \"sporsmal\": [\n" +
            "            {\n" +
            "                \"id\": \"55\",\n" +
            "                \"tag\": \"ANSVARSERKLARING\",\n" +
            "                \"sporsmalstekst\": \"Jeg vet at dersom jeg gir uriktige opplysninger, eller holder tilbake opplysninger som har betydning for min rett til sykepenger, kan pengene holdes tilbake eller kreves tilbake, og/eller det kan medføre straffeansvar. Jeg er også klar over at jeg må melde fra til NAV dersom jeg i sykmeldingsperioden satt i varetekt, sonet straff eller var under forvaring.\",\n" +
            "                \"undertekst\": null,\n" +
            "                \"svartype\": \"CHECKBOX_PANEL\",\n" +
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
            "                \"id\": \"56\",\n" +
            "                \"tag\": \"EGENMELDINGER\",\n" +
            "                \"sporsmalstekst\": \"Vi har registrert at du ble sykmeldt tirsdag 16. oktober 2018. Brukte du egenmeldinger og/eller var du sykmeldt i perioden 30. september - 15. oktober 2018?\",\n" +
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
            "                        \"id\": \"57\",\n" +
            "                        \"tag\": \"EGENMELDINGER_NAR\",\n" +
            "                        \"sporsmalstekst\": \"Hvilke dager før 16. oktober 2018 var du borte fra jobb?\",\n" +
            "                        \"undertekst\": null,\n" +
            "                        \"svartype\": \"PERIODER\",\n" +
            "                        \"min\": \"2018-04-16\",\n" +
            "                        \"max\": \"2018-10-15\",\n" +
            "                        \"kriterieForVisningAvUndersporsmal\": null,\n" +
            "                        \"svar\": [\n" +
            "                            {\n" +
            "                                \"verdi\": \"{\\\"fom\\\":\\\"2018-10-08\\\",\\\"tom\\\":\\\"2018-10-10\\\"}\"\n" +
            "                            }\n" +
            "                        ],\n" +
            "                        \"undersporsmal\": []\n" +
            "                    }\n" +
            "                ]\n" +
            "            },\n" +
            "            {\n" +
            "                \"id\": \"58\",\n" +
            "                \"tag\": \"TILBAKE_I_ARBEID\",\n" +
            "                \"sporsmalstekst\": \"Var du tilbake i fullt arbeid hos ARBEIDSGIVER A/S før 25. oktober 2018?\",\n" +
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
            "                        \"id\": \"59\",\n" +
            "                        \"tag\": \"TILBAKE_NAR\",\n" +
            "                        \"sporsmalstekst\": \"Fra hvilken dato ble arbeidet gjenopptatt?\",\n" +
            "                        \"undertekst\": null,\n" +
            "                        \"svartype\": \"DATO\",\n" +
            "                        \"min\": \"2018-10-16\",\n" +
            "                        \"max\": \"2018-10-24\",\n" +
            "                        \"kriterieForVisningAvUndersporsmal\": null,\n" +
            "                        \"svar\": [\n" +
            "                            {\n" +
            "                                \"verdi\": \"2018-10-23\"\n" +
            "                            }\n" +
            "                        ],\n" +
            "                        \"undersporsmal\": []\n" +
            "                    }\n" +
            "                ]\n" +
            "            },\n" +
            "            {\n" +
            "                \"id\": \"60\",\n" +
            "                \"tag\": \"JOBBET_DU_100_PROSENT_0\",\n" +
            "                \"sporsmalstekst\": \"I perioden 16. - 20. oktober 2018 var du 100 % sykmeldt fra ARBEIDSGIVER A/S. Jobbet du noe i denne perioden?\",\n" +
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
            "                        \"id\": \"61\",\n" +
            "                        \"tag\": \"HVOR_MANGE_TIMER_PER_UKE_0\",\n" +
            "                        \"sporsmalstekst\": \"Hvor mange timer jobbet du per uke før du ble sykmeldt?\",\n" +
            "                        \"undertekst\": \"timer per uke\",\n" +
            "                        \"svartype\": \"TALL\",\n" +
            "                        \"min\": \"1\",\n" +
            "                        \"max\": \"150\",\n" +
            "                        \"kriterieForVisningAvUndersporsmal\": null,\n" +
            "                        \"svar\": [\n" +
            "                            {\n" +
            "                                \"verdi\": \"37,5\"\n" +
            "                            }\n" +
            "                        ],\n" +
            "                        \"undersporsmal\": []\n" +
            "                    },\n" +
            "                    {\n" +
            "                        \"id\": \"62\",\n" +
            "                        \"tag\": \"HVOR_MYE_HAR_DU_JOBBET_0\",\n" +
            "                        \"sporsmalstekst\": \"Hvor mye jobbet du totalt 16. - 20. oktober 2018 hos ARBEIDSGIVER A/S?\",\n" +
            "                        \"undertekst\": null,\n" +
            "                        \"svartype\": \"RADIO_GRUPPE_TIMER_PROSENT\",\n" +
            "                        \"min\": null,\n" +
            "                        \"max\": null,\n" +
            "                        \"kriterieForVisningAvUndersporsmal\": null,\n" +
            "                        \"svar\": [],\n" +
            "                        \"undersporsmal\": [\n" +
            "                            {\n" +
            "                                \"id\": \"63\",\n" +
            "                                \"tag\": \"HVOR_MYE_PROSENT_0\",\n" +
            "                                \"sporsmalstekst\": \"prosent\",\n" +
            "                                \"undertekst\": null,\n" +
            "                                \"svartype\": \"RADIO\",\n" +
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
            "                                        \"id\": \"64\",\n" +
            "                                        \"tag\": \"HVOR_MYE_PROSENT_VERDI_0\",\n" +
            "                                        \"sporsmalstekst\": null,\n" +
            "                                        \"undertekst\": \"prosent\",\n" +
            "                                        \"svartype\": \"TALL\",\n" +
            "                                        \"min\": \"1\",\n" +
            "                                        \"max\": \"99\",\n" +
            "                                        \"kriterieForVisningAvUndersporsmal\": null,\n" +
            "                                        \"svar\": [\n" +
            "                                            {\n" +
            "                                                \"verdi\": \"79\"\n" +
            "                                            }\n" +
            "                                        ],\n" +
            "                                        \"undersporsmal\": []\n" +
            "                                    }\n" +
            "                                ]\n" +
            "                            },\n" +
            "                            {\n" +
            "                                \"id\": \"65\",\n" +
            "                                \"tag\": \"HVOR_MYE_TIMER_0\",\n" +
            "                                \"sporsmalstekst\": \"timer\",\n" +
            "                                \"undertekst\": null,\n" +
            "                                \"svartype\": \"RADIO\",\n" +
            "                                \"min\": null,\n" +
            "                                \"max\": null,\n" +
            "                                \"kriterieForVisningAvUndersporsmal\": \"CHECKED\",\n" +
            "                                \"svar\": [],\n" +
            "                                \"undersporsmal\": [\n" +
            "                                    {\n" +
            "                                        \"id\": \"66\",\n" +
            "                                        \"tag\": \"HVOR_MYE_TIMER_VERDI_0\",\n" +
            "                                        \"sporsmalstekst\": null,\n" +
            "                                        \"undertekst\": \"timer totalt\",\n" +
            "                                        \"svartype\": \"TALL\",\n" +
            "                                        \"min\": \"1\",\n" +
            "                                        \"max\": \"107\",\n" +
            "                                        \"kriterieForVisningAvUndersporsmal\": null,\n" +
            "                                        \"svar\": [],\n" +
            "                                        \"undersporsmal\": []\n" +
            "                                    }\n" +
            "                                ]\n" +
            "                            }\n" +
            "                        ]\n" +
            "                    }\n" +
            "                ]\n" +
            "            },\n" +
            "            {\n" +
            "                \"id\": \"67\",\n" +
            "                \"tag\": \"JOBBET_DU_GRADERT_1\",\n" +
            "                \"sporsmalstekst\": \"I perioden 21. - 24. oktober 2018 skulle du jobbe 60 % av ditt normale arbeid hos ARBEIDSGIVER A/S. Jobbet du mer enn dette?\",\n" +
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
            "                        \"id\": \"68\",\n" +
            "                        \"tag\": \"HVOR_MANGE_TIMER_PER_UKE_1\",\n" +
            "                        \"sporsmalstekst\": \"Hvor mange timer jobbet du per uke før du ble sykmeldt?\",\n" +
            "                        \"undertekst\": \"timer per uke\",\n" +
            "                        \"svartype\": \"TALL\",\n" +
            "                        \"min\": \"1\",\n" +
            "                        \"max\": \"150\",\n" +
            "                        \"kriterieForVisningAvUndersporsmal\": null,\n" +
            "                        \"svar\": [\n" +
            "                            {\n" +
            "                                \"verdi\": \"37.5\"\n" +
            "                            }\n" +
            "                        ],\n" +
            "                        \"undersporsmal\": []\n" +
            "                    },\n" +
            "                    {\n" +
            "                        \"id\": \"69\",\n" +
            "                        \"tag\": \"HVOR_MYE_HAR_DU_JOBBET_1\",\n" +
            "                        \"sporsmalstekst\": \"Hvor mye jobbet du totalt 21. - 24. oktober 2018 hos ARBEIDSGIVER A/S?\",\n" +
            "                        \"undertekst\": null,\n" +
            "                        \"svartype\": \"RADIO_GRUPPE_TIMER_PROSENT\",\n" +
            "                        \"min\": null,\n" +
            "                        \"max\": null,\n" +
            "                        \"kriterieForVisningAvUndersporsmal\": null,\n" +
            "                        \"svar\": [],\n" +
            "                        \"undersporsmal\": [\n" +
            "                            {\n" +
            "                                \"id\": \"70\",\n" +
            "                                \"tag\": \"HVOR_MYE_PROSENT_1\",\n" +
            "                                \"sporsmalstekst\": \"prosent\",\n" +
            "                                \"undertekst\": null,\n" +
            "                                \"svartype\": \"RADIO\",\n" +
            "                                \"min\": null,\n" +
            "                                \"max\": null,\n" +
            "                                \"kriterieForVisningAvUndersporsmal\": \"CHECKED\",\n" +
            "                                \"svar\": [],\n" +
            "                                \"undersporsmal\": [\n" +
            "                                    {\n" +
            "                                        \"id\": \"71\",\n" +
            "                                        \"tag\": \"HVOR_MYE_PROSENT_VERDI_1\",\n" +
            "                                        \"sporsmalstekst\": null,\n" +
            "                                        \"undertekst\": \"prosent\",\n" +
            "                                        \"svartype\": \"TALL\",\n" +
            "                                        \"min\": \"61\",\n" +
            "                                        \"max\": \"99\",\n" +
            "                                        \"kriterieForVisningAvUndersporsmal\": null,\n" +
            "                                        \"svar\": [],\n" +
            "                                        \"undersporsmal\": []\n" +
            "                                    }\n" +
            "                                ]\n" +
            "                            },\n" +
            "                            {\n" +
            "                                \"id\": \"72\",\n" +
            "                                \"tag\": \"HVOR_MYE_TIMER_1\",\n" +
            "                                \"sporsmalstekst\": \"timer\",\n" +
            "                                \"undertekst\": null,\n" +
            "                                \"svartype\": \"RADIO\",\n" +
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
            "                                        \"id\": \"73\",\n" +
            "                                        \"tag\": \"HVOR_MYE_TIMER_VERDI_1\",\n" +
            "                                        \"sporsmalstekst\": null,\n" +
            "                                        \"undertekst\": \"timer totalt\",\n" +
            "                                        \"svartype\": \"TALL\",\n" +
            "                                        \"min\": \"1\",\n" +
            "                                        \"max\": \"86\",\n" +
            "                                        \"kriterieForVisningAvUndersporsmal\": null,\n" +
            "                                        \"svar\": [\n" +
            "                                            {\n" +
            "                                                \"verdi\": \"66\"\n" +
            "                                            }\n" +
            "                                        ],\n" +
            "                                        \"undersporsmal\": []\n" +
            "                                    }\n" +
            "                                ]\n" +
            "                            }\n" +
            "                        ]\n" +
            "                    }\n" +
            "                ]\n" +
            "            },\n" +
            "            {\n" +
            "                \"id\": \"74\",\n" +
            "                \"tag\": \"FERIE_PERMISJON_UTLAND\",\n" +
            "                \"sporsmalstekst\": \"Har du hatt ferie, permisjon eller oppholdt deg utenfor Norge i perioden 16. - 24. oktober 2018?\",\n" +
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
            "                        \"id\": \"75\",\n" +
            "                        \"tag\": \"FERIE_PERMISJON_UTLAND_HVA\",\n" +
            "                        \"sporsmalstekst\": \"Kryss av alt som gjelder deg:\",\n" +
            "                        \"undertekst\": null,\n" +
            "                        \"svartype\": \"CHECKBOX_GRUPPE\",\n" +
            "                        \"min\": null,\n" +
            "                        \"max\": null,\n" +
            "                        \"kriterieForVisningAvUndersporsmal\": null,\n" +
            "                        \"svar\": [],\n" +
            "                        \"undersporsmal\": [\n" +
            "                            {\n" +
            "                                \"id\": \"76\",\n" +
            "                                \"tag\": \"FERIE\",\n" +
            "                                \"sporsmalstekst\": \"Jeg tok ut ferie\",\n" +
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
            "                                        \"id\": \"77\",\n" +
            "                                        \"tag\": \"FERIE_NAR\",\n" +
            "                                        \"sporsmalstekst\": null,\n" +
            "                                        \"undertekst\": null,\n" +
            "                                        \"svartype\": \"PERIODER\",\n" +
            "                                        \"min\": \"2018-10-16\",\n" +
            "                                        \"max\": \"2018-10-24\",\n" +
            "                                        \"kriterieForVisningAvUndersporsmal\": null,\n" +
            "                                        \"svar\": [\n" +
            "                                            {\n" +
            "                                                \"verdi\": \"{\\\"fom\\\":\\\"2018-10-18\\\",\\\"tom\\\":\\\"2018-10-20\\\"}\"\n" +
            "                                            }\n" +
            "                                        ],\n" +
            "                                        \"undersporsmal\": []\n" +
            "                                    }\n" +
            "                                ]\n" +
            "                            },\n" +
            "                            {\n" +
            "                                \"id\": \"78\",\n" +
            "                                \"tag\": \"PERMISJON\",\n" +
            "                                \"sporsmalstekst\": \"Jeg hadde permisjon\",\n" +
            "                                \"undertekst\": null,\n" +
            "                                \"svartype\": \"CHECKBOX\",\n" +
            "                                \"min\": null,\n" +
            "                                \"max\": null,\n" +
            "                                \"kriterieForVisningAvUndersporsmal\": \"CHECKED\",\n" +
            "                                \"svar\": [],\n" +
            "                                \"undersporsmal\": [\n" +
            "                                    {\n" +
            "                                        \"id\": \"79\",\n" +
            "                                        \"tag\": \"PERMISJON_NAR\",\n" +
            "                                        \"sporsmalstekst\": null,\n" +
            "                                        \"undertekst\": null,\n" +
            "                                        \"svartype\": \"PERIODER\",\n" +
            "                                        \"min\": \"2018-10-16\",\n" +
            "                                        \"max\": \"2018-10-24\",\n" +
            "                                        \"kriterieForVisningAvUndersporsmal\": null,\n" +
            "                                        \"svar\": [],\n" +
            "                                        \"undersporsmal\": []\n" +
            "                                    }\n" +
            "                                ]\n" +
            "                            },\n" +
            "                            {\n" +
            "                                \"id\": \"80\",\n" +
            "                                \"tag\": \"UTLAND\",\n" +
            "                                \"sporsmalstekst\": \"Jeg var utenfor Norge\",\n" +
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
            "                                        \"id\": \"81\",\n" +
            "                                        \"tag\": \"UTLAND_NAR\",\n" +
            "                                        \"sporsmalstekst\": null,\n" +
            "                                        \"undertekst\": null,\n" +
            "                                        \"svartype\": \"PERIODER\",\n" +
            "                                        \"min\": \"2018-10-16\",\n" +
            "                                        \"max\": \"2018-10-24\",\n" +
            "                                        \"kriterieForVisningAvUndersporsmal\": null,\n" +
            "                                        \"svar\": [\n" +
            "                                            {\n" +
            "                                                \"verdi\": \"{\\\"fom\\\":\\\"2018-10-17\\\",\\\"tom\\\":\\\"2018-10-18\\\"}\"\n" +
            "                                            },\n" +
            "                                            {\n" +
            "                                                \"verdi\": \"{\\\"fom\\\":\\\"2018-10-20\\\",\\\"tom\\\":\\\"2018-10-22\\\"}\"\n" +
            "                                            }\n" +
            "                                        ],\n" +
            "                                        \"undersporsmal\": []\n" +
            "                                    }\n" +
            "                                ]\n" +
            "                            }\n" +
            "                        ]\n" +
            "                    }\n" +
            "                ]\n" +
            "            },\n" +
            "            {\n" +
            "                \"id\": \"82\",\n" +
            "                \"tag\": \"ANDRE_INNTEKTSKILDER\",\n" +
            "                \"sporsmalstekst\": \"Har du andre inntektskilder, eller jobber du for andre enn ARBEIDSGIVER A/S?\",\n" +
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
            "                        \"id\": \"83\",\n" +
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
            "                                \"id\": \"84\",\n" +
            "                                \"tag\": \"INNTEKTSKILDE_ANDRE_ARBEIDSFORHOLD\",\n" +
            "                                \"sporsmalstekst\": \"Andre arbeidsforhold\",\n" +
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
            "                                        \"id\": \"85\",\n" +
            "                                        \"tag\": \"INNTEKTSKILDE_ANDRE_ARBEIDSFORHOLD_ER_DU_SYKMELDT\",\n" +
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
            "                                \"id\": \"86\",\n" +
            "                                \"tag\": \"INNTEKTSKILDE_SELVSTENDIG\",\n" +
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
            "                                        \"id\": \"87\",\n" +
            "                                        \"tag\": \"INNTEKTSKILDE_SELVSTENDIG_ER_DU_SYKMELDT\",\n" +
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
            "                                \"id\": \"88\",\n" +
            "                                \"tag\": \"INNTEKTSKILDE_SELVSTENDIG_DAGMAMMA\",\n" +
            "                                \"sporsmalstekst\": \"Selvstendig næringsdrivende dagmamma\",\n" +
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
            "                                        \"id\": \"89\",\n" +
            "                                        \"tag\": \"INNTEKTSKILDE_SELVSTENDIG_DAGMAMMA_ER_DU_SYKMELDT\",\n" +
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
            "                                \"id\": \"90\",\n" +
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
            "                                        \"id\": \"91\",\n" +
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
            "                                \"id\": \"92\",\n" +
            "                                \"tag\": \"INNTEKTSKILDE_FRILANSER\",\n" +
            "                                \"sporsmalstekst\": \"Frilanser\",\n" +
            "                                \"undertekst\": null,\n" +
            "                                \"svartype\": \"CHECKBOX\",\n" +
            "                                \"min\": null,\n" +
            "                                \"max\": null,\n" +
            "                                \"kriterieForVisningAvUndersporsmal\": \"CHECKED\",\n" +
            "                                \"svar\": [],\n" +
            "                                \"undersporsmal\": [\n" +
            "                                    {\n" +
            "                                        \"id\": \"93\",\n" +
            "                                        \"tag\": \"INNTEKTSKILDE_FRILANSER_ER_DU_SYKMELDT\",\n" +
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
            "                                \"id\": \"94\",\n" +
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
            "                \"id\": \"95\",\n" +
            "                \"tag\": \"UTDANNING\",\n" +
            "                \"sporsmalstekst\": \"Har du vært under utdanning i løpet av perioden 16. - 24. oktober 2018?\",\n" +
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
            "                        \"id\": \"96\",\n" +
            "                        \"tag\": \"UTDANNING_START\",\n" +
            "                        \"sporsmalstekst\": \"Når startet du på utdanningen?\",\n" +
            "                        \"undertekst\": null,\n" +
            "                        \"svartype\": \"DATO\",\n" +
            "                        \"min\": null,\n" +
            "                        \"max\": \"2018-10-24\",\n" +
            "                        \"kriterieForVisningAvUndersporsmal\": null,\n" +
            "                        \"svar\": [\n" +
            "                            {\n" +
            "                                \"verdi\": \"2018-10-19\"\n" +
            "                            }\n" +
            "                        ],\n" +
            "                        \"undersporsmal\": []\n" +
            "                    },\n" +
            "                    {\n" +
            "                        \"id\": \"97\",\n" +
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
            "                \"id\": \"98\",\n" +
            "                \"tag\": \"VAER_KLAR_OVER_AT\",\n" +
            "                \"sporsmalstekst\": \"Vær klar over at:\",\n" +
            "                \"undertekst\": \"<ul><li>rett til sykepenger forutsetter at du er borte fra arbeid på grunn av egen sykdom. Sosiale eller økonomiske problemer gir ikke rett til sykepenger</li><li>du kan miste retten til sykepenger hvis du uten rimelig grunn nekter å opplyse om egen funksjonsevne eller nekter å ta imot tilbud om behandling og/eller tilrettelegging</li><li>sykepenger utbetales i maksimum 52 uker, også for gradert (delvis) sykmelding</li><li>fristen for å søke sykepenger er som hovedregel 3 måneder</li></ul>\",\n" +
            "                \"svartype\": \"IKKE_RELEVANT\",\n" +
            "                \"min\": null,\n" +
            "                \"max\": null,\n" +
            "                \"kriterieForVisningAvUndersporsmal\": null,\n" +
            "                \"svar\": [],\n" +
            "                \"undersporsmal\": []\n" +
            "            },\n" +
            "            {\n" +
            "                \"id\": \"99\",\n" +
            "                \"tag\": \"BEKREFT_OPPLYSNINGER\",\n" +
            "                \"sporsmalstekst\": \"Jeg har lest all informasjonen jeg har fått i søknaden og bekrefter at opplysningene jeg har gitt er korrekte.\",\n" +
            "                \"undertekst\": null,\n" +
            "                \"svartype\": \"CHECKBOX_PANEL\",\n" +
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
            "                \"id\": \"100\",\n" +
            "                \"tag\": \"BETALER_ARBEIDSGIVER\",\n" +
            "                \"sporsmalstekst\": \"Betaler arbeidsgiveren lønnen din når du er syk?\",\n" +
            "                \"undertekst\": null,\n" +
            "                \"svartype\": \"RADIO_GRUPPE\",\n" +
            "                \"min\": null,\n" +
            "                \"max\": null,\n" +
            "                \"kriterieForVisningAvUndersporsmal\": null,\n" +
            "                \"svar\": [],\n" +
            "                \"undersporsmal\": [\n" +
            "                    {\n" +
            "                        \"id\": \"101\",\n" +
            "                        \"tag\": \"BETALER_ARBEIDSGIVER_JA\",\n" +
            "                        \"sporsmalstekst\": \"Ja\",\n" +
            "                        \"undertekst\": \"Arbeidsgiveren din mottar kopi av søknaden du sender til NAV.\",\n" +
            "                        \"svartype\": \"RADIO\",\n" +
            "                        \"min\": null,\n" +
            "                        \"max\": null,\n" +
            "                        \"kriterieForVisningAvUndersporsmal\": null,\n" +
            "                        \"svar\": [],\n" +
            "                        \"undersporsmal\": []\n" +
            "                    },\n" +
            "                    {\n" +
            "                        \"id\": \"102\",\n" +
            "                        \"tag\": \"BETALER_ARBEIDSGIVER_NEI\",\n" +
            "                        \"sporsmalstekst\": \"Nei\",\n" +
            "                        \"undertekst\": \"Søknaden sendes til NAV. Arbeidsgiveren din får ikke kopi.\",\n" +
            "                        \"svartype\": \"RADIO\",\n" +
            "                        \"min\": null,\n" +
            "                        \"max\": null,\n" +
            "                        \"kriterieForVisningAvUndersporsmal\": null,\n" +
            "                        \"svar\": [],\n" +
            "                        \"undersporsmal\": []\n" +
            "                    },\n" +
            "                    {\n" +
            "                        \"id\": \"103\",\n" +
            "                        \"tag\": \"BETALER_ARBEIDSGIVER_VET_IKKE\",\n" +
            "                        \"sporsmalstekst\": \"Vet ikke\",\n" +
            "                        \"undertekst\": \"Siden du ikke vet svaret på dette spørsmålet, vil arbeidsgiveren din motta en kopi av søknaden du sender til NAV.\",\n" +
            "                        \"svartype\": \"RADIO\",\n" +
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

    public static final String beskrivelseArbeidstakerMangeSvar = "Søknad om sykepenger for perioden 16.10.2018 - 24.10.2018\n" +
            "\n" +
            "Arbeidsgiver: ARBEIDSGIVER A/S" +
            "\n" +
            "\n" +
            "Periode 1:\n" +
            "16.10.2018 - 20.10.2018\n" +
            "Grad: 100\n" +
            "Oppgitt faktisk arbeidsgrad: 79\n" +
            "\n" +
            "Periode 2:\n" +
            "21.10.2018 - 24.10.2018\n" +
            "Grad: 40\n" +
            "Oppgitt faktisk arbeidsgrad: 440\n" +
            "\n" +
            "Vi har registrert at du ble sykmeldt tirsdag 16. oktober 2018. Brukte du egenmeldinger og/eller var du sykmeldt i perioden 30. september - 15. oktober 2018?\n" +
            "Ja\n" +
            "    Hvilke dager før 16. oktober 2018 var du borte fra jobb?\n" +
            "    08.10.2018 - 10.10.2018\n" +
            "\n" +
            "Var du tilbake i fullt arbeid hos ARBEIDSGIVER A/S før 25. oktober 2018?\n" +
            "Ja\n" +
            "    Fra hvilken dato ble arbeidet gjenopptatt?\n" +
            "    23.10.2018\n" +
            "\n" +
            "I perioden 16. - 20. oktober 2018 var du 100 % sykmeldt fra ARBEIDSGIVER A/S. Jobbet du noe i denne perioden?\n" +
            "Ja\n" +
            "    Hvor mange timer jobbet du per uke før du ble sykmeldt?\n" +
            "    37,5 timer per uke\n" +
            "\n" +
            "    Hvor mye jobbet du totalt 16. - 20. oktober 2018 hos ARBEIDSGIVER A/S?\n" +
            "    79 prosent\n" +
            "\n" +
            "I perioden 21. - 24. oktober 2018 skulle du jobbe 60 % av ditt normale arbeid hos ARBEIDSGIVER A/S. Jobbet du mer enn dette?\n" +
            "Ja\n" +
            "    Hvor mange timer jobbet du per uke før du ble sykmeldt?\n" +
            "    37.5 timer per uke\n" +
            "\n" +
            "    Hvor mye jobbet du totalt 21. - 24. oktober 2018 hos ARBEIDSGIVER A/S?\n" +
            "    66 timer totalt\n" +
            "\n" +
            "Har du hatt ferie, permisjon eller oppholdt deg utenfor Norge i perioden 16. - 24. oktober 2018?\n" +
            "Ja\n" +
            "    Kryss av alt som gjelder deg:\n" +
            "        Jeg tok ut ferie\n" +
            "            18.10.2018 - 20.10.2018\n" +
            "\n" +
            "        Jeg var utenfor Norge\n" +
            "            17.10.2018 - 18.10.2018\n" +
            "            20.10.2018 - 22.10.2018\n" +
            "\n" +
            "Har du andre inntektskilder, eller jobber du for andre enn ARBEIDSGIVER A/S?\n" +
            "Ja\n" +
            "    Hvilke andre inntektskilder har du?\n" +
            "        Andre arbeidsforhold\n" +
            "            Er du sykmeldt fra dette?\n" +
            "            Ja\n" +
            "\n" +
            "        Selvstendig næringsdrivende\n" +
            "            Er du sykmeldt fra dette?\n" +
            "            Ja\n" +
            "\n" +
            "        Selvstendig næringsdrivende dagmamma\n" +
            "            Er du sykmeldt fra dette?\n" +
            "            Nei\n" +
            "\n" +
            "Har du vært under utdanning i løpet av perioden 16. - 24. oktober 2018?\n" +
            "Ja\n" +
            "    Når startet du på utdanningen?\n" +
            "    19.10.2018\n" +
            "\n" +
            "    Er utdanningen et fulltidsstudium?\n" +
            "    Nei\n" +
            "\n" +
            "Betaler arbeidsgiveren lønnen din når du er syk?\n" +
            "Vet ikke";
}
