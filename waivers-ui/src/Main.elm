module Main exposing (Flags, Model, Msg, main)

import Browser
import Browser.Navigation as Nav
import Html as H
import Html.Attributes as A
import Page.Admin as PageAdmin
import Page.Index as PageIndex
import Page.Sign as PageSign
import Page.Waiver as PageWaiver
import Route exposing (Route)
import Url
import Time
import Task


type alias Flags =
    { version : String }


main : Program Flags Model Msg
main =
    Browser.application
        { init = init
        , view = view
        , update = update
        , subscriptions = subscriptions
        , onUrlChange = UrlChanged
        , onUrlRequest = LinkClicked
        }


type Model
    = Model
        { key : Nav.Key
        , url : Url.Url
        , route : Maybe Route
        , version : String
        , currentYear : Maybe Int
        , pageWaiver : Maybe PageWaiver.Model
        , pageSign : Maybe PageSign.Model
        }


type Msg
    = LinkClicked Browser.UrlRequest
    | UrlChanged Url.Url
    | PageWaiverMsg PageWaiver.Msg
    | PageSignMsg PageSign.Msg
    | GotCurrentYear Int


init : Flags -> Url.Url -> Nav.Key -> ( Model, Cmd Msg )
init flags url key =
    let
        route =
            Route.fromUrl url

        ( pageWaiverModel, pageWaiverCmd ) =
            case route of
                Just (Route.RouteWaiver slug) ->
                    let
                        ( waiverModel, waiverCmd ) =
                            PageWaiver.init slug
                    in
                    ( Just waiverModel, Cmd.map PageWaiverMsg waiverCmd )

                _ ->
                    ( Nothing, Cmd.none )

        ( pageSignModel, pageSignCmd ) =
            case route of
                Just (Route.RouteSign signParams) ->
                    let
                        signModel = PageSign.init signParams.signatureId signParams.pdfUrl
                    in
                    ( Just signModel, Cmd.none )

                _ ->
                    ( Nothing, Cmd.none )

        appModel =
            Model
                { key = key
                , url = url
                , route = route
                , version = flags.version
                , currentYear = Nothing
                , pageWaiver = pageWaiverModel
                , pageSign = pageSignModel
                }
        
        getCurrentYearCmd =
            Task.perform 
                (\posix -> GotCurrentYear (Time.toYear Time.utc posix))
                Time.now
    in
    ( appModel, Cmd.batch [ pageWaiverCmd, pageSignCmd, getCurrentYearCmd ] )


update : Msg -> Model -> ( Model, Cmd Msg )
update msg (Model model) =
    case msg of
        LinkClicked urlRequest ->
            case urlRequest of
                Browser.Internal url ->
                    ( Model model, Nav.pushUrl model.key (Url.toString url) )

                Browser.External href ->
                    ( Model model, Nav.load href )

        UrlChanged url ->
            let
                route =
                    Route.fromUrl url

                ( pageWaiverModel, pageWaiverCmd ) =
                    case route of
                        Just (Route.RouteWaiver slug) ->
                            let
                                ( waiverModel, waiverCmd ) =
                                    PageWaiver.init slug
                            in
                            ( Just waiverModel, Cmd.map PageWaiverMsg waiverCmd )

                        _ ->
                            ( Nothing, Cmd.none )

                ( pageSignModel, pageSignCmd ) =
                    case route of
                        Just (Route.RouteSign signParams) ->
                            let
                                signModel = PageSign.init signParams.signatureId signParams.pdfUrl
                            in
                            ( Just signModel, Cmd.none )

                        _ ->
                            ( Nothing, Cmd.none )

                updatedModel =
                    { model | url = url, route = route, pageWaiver = pageWaiverModel, pageSign = pageSignModel }
            in
            ( Model updatedModel, Cmd.batch [ pageWaiverCmd, pageSignCmd ] )

        PageWaiverMsg pageMsg ->
            case model.pageWaiver of
                Just pageModel ->
                    let
                        ( newPageModel, pageCmd ) =
                            PageWaiver.update pageMsg pageModel
                    in
                    ( Model { model | pageWaiver = Just newPageModel }, Cmd.map PageWaiverMsg pageCmd )

                Nothing ->
                    ( Model model, Cmd.none )

        PageSignMsg pageMsg ->
            case model.pageSign of
                Just pageModel ->
                    let
                        ( newPageModel, pageCmd ) =
                            PageSign.update pageMsg pageModel
                    in
                    ( Model { model | pageSign = Just newPageModel }, Cmd.map PageSignMsg pageCmd )

                Nothing ->
                    ( Model model, Cmd.none )

        GotCurrentYear year ->
            ( Model { model | currentYear = Just year }, Cmd.none )


subscriptions : Model -> Sub Msg
subscriptions _ =
    Sub.none


view : Model -> Browser.Document Msg
view (Model model) =
    { title = "Waivers"
    , body =
        [ viewContent model.route model.pageWaiver model.pageSign model.currentYear
        ]
    }


viewContent : Maybe Route -> Maybe PageWaiver.Model -> Maybe PageSign.Model -> Maybe Int -> H.Html Msg
viewContent maybeRoute pageWaiverModel pageSignModel currentYear =
    case maybeRoute of
        Nothing ->
            H.div [ A.class "container mx-auto px-4 py-8" ]
                [ H.h1 [ A.class "text-2xl font-bold text-gray-900" ] [ H.text "Page Not Found" ]
                , H.p [ A.class "mt-4 text-gray-600" ] [ H.text "The page you're looking for doesn't exist." ]
                ]

        Just route ->
            case route of
                Route.RouteHome ->
                    PageIndex.view currentYear

                Route.RouteWaiver _ ->
                    case pageWaiverModel of
                        Just model ->
                            H.map PageWaiverMsg (PageWaiver.view model currentYear)

                        Nothing ->
                            H.div [ A.class "container mx-auto px-4 py-8" ]
                                [ H.text "Loading..." ]

                Route.RouteSign _ ->
                    case pageSignModel of
                        Just model ->
                            H.map PageSignMsg (PageSign.view model)

                        Nothing ->
                            H.div [ A.class "container mx-auto px-4 py-8" ]
                                [ H.text "Loading..." ]

                Route.RouteAdmin ->
                    PageAdmin.view currentYear

                Route.RouteAdminProjects ->
                    H.div [ A.class "container mx-auto px-4 py-8" ]
                        [ H.h1 [ A.class "text-2xl font-bold text-gray-900" ] [ H.text "Manage Projects" ]
                        ]

                Route.RouteAdminProjectDetail projectId ->
                    H.div [ A.class "container mx-auto px-4 py-8" ]
                        [ H.h1 [ A.class "text-2xl font-bold text-gray-900" ] [ H.text ("Project: " ++ projectId) ]
                        ]

                Route.RouteAdminSignatures ->
                    H.div [ A.class "container mx-auto px-4 py-8" ]
                        [ H.h1 [ A.class "text-2xl font-bold text-gray-900" ] [ H.text "Signatures" ]
                        ]

                Route.RouteSignatureStatus signatureId ->
                    H.div [ A.class "container mx-auto px-4 py-8" ]
                        [ H.h1 [ A.class "text-2xl font-bold text-gray-900" ] [ H.text ("Signature Status: " ++ signatureId) ]
                        ]