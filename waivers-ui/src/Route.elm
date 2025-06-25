module Route exposing (Route(..), SignParams, fromUrl)

import Url exposing (Url)
import Url.Parser exposing (..)
import Url.Parser.Query as Query


type alias SignParams =
    { signatureId : String
    , pdfUrl : Maybe String
    }


type Route
    = RouteHome
    | RouteWaiver String
    | RouteSign SignParams
    | RouteAdmin
    | RouteAdminProjects
    | RouteAdminProjectDetail String
    | RouteAdminSignatures
    | RouteSignatureStatus String


fromUrl : Url -> Maybe Route
fromUrl url =
    parse matchRoute url


matchRoute : Parser (Route -> a) a
matchRoute =
    oneOf
        [ map RouteHome top
        , map RouteWaiver (s "waiver" </> string)
        , map (\signatureId pdfUrl -> RouteSign { signatureId = signatureId, pdfUrl = Maybe.andThen Url.percentDecode pdfUrl })
            (s "sign" </> string <?> Query.string "pdf")
        , map RouteAdmin (s "admin")
        , map RouteAdminProjects (s "admin" </> s "projects")
        , map RouteAdminProjectDetail (s "admin" </> s "projects" </> string)
        , map RouteAdminSignatures (s "admin" </> s "signatures")
        , map RouteSignatureStatus (s "signatures" </> string)
        ]
